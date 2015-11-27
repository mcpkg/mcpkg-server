# Package Server

## Package specification

Packages are specified by a file named `<sha256-hash>-<package-name>.json`, 
where `<sha256-hash>` is a hash of the file's contents and `<package-name>` is
the same as the `name` field of the file. Package files are JSON files,
following the specification in IETF RFC 7159.

The contents of the JSON file are as follows:

```json
{ 
  "name":          "<name>",
  "repo":          "<repo>",
  "synopsis":      "<synopsis>",
  "description":   "<description>",
  "builder":       "<builder>",
  "license":       "<spdx>",
  "configuration": <configuration>
}
```

- `<name>` is the package name, which comprises up to 128 bytes of UTF-8.
- `<repo>` is an HTTPS URL to a Git repository.
  It can contain up to one kilobyte of ASCII (using Punycode when necessary).
- `<synopsis>` is a short description of the package -- less than 80 grapheme
  clusters. It can contain up to 512 bytes of UTF-8.
- `<description>` is a potentially long description of the package.
  It can contain up to two kilobytes of UTF-8.
- `<builder>` is the name of a builder.
  Currently the only options are `nix`, `makefile`, `maven`, and `gradle`.
- `<spdx>` is the name of a license in [SPDX][spdx-site] format.
  For example, for the GNU GPLv3 license, you would write `GPL-3.0`.
  For more information, read [this specification][spdx-specification].
- `<configuration>` is a JSON object containing any remaining configuration
  information relevant to the package. It will be left empty for now.

This JSON format will be specified by a [JSON Schema][json-schema].

 

```json
{
    "id": "http://some.site.somewhere/entry-schema#",
    "$schema": "http://json-schema.org/draft-04/schema#",
    "description": "Schema for an mcpkg package file.",
    "type": "object",
    "required": [ "name", "repo", "synopsis", "builder", "license" ],
    "oneOf": {}
    "properties": {
        "name": {
            "maxLength": 
        },
        "storage": {
            "type": "object",
            "oneOf": [ { "$ref": "#/definitions/diskDevice" }
                     , { "$ref": "#/definitions/diskUUID"   }
                     , { "$ref": "#/definitions/nfs"        }
                     , { "$ref": "#/definitions/tmpfs"      } ]
        },
        "builder": { "enum": [ "nix", "makefile", "maven", "gradle" ] },
        "options": {
            "type": "array",
            "minItems": 1,
            "items": { "type": "string" },
            "uniqueItems": true
        },
        "readonly": { "type": "boolean" }
    },
    "definitions": {
        "diskDevice": {
            "properties": {
                "type": { "enum": [ "disk" ] },
                "device": {
                    "type": "string",
                    "pattern": "^/dev/[^/]+(/[^/]+)*$"
                }
            },
            "required": [ "type", "device" ],
            "additionalProperties": false
        },
        "diskUUID": {
            "properties": {
                "type": { "enum": [ "disk" ] },
                "label": {
                    "type": "string",
                    "pattern": "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$"
                }
            },
            "required": [ "type", "label" ],
            "additionalProperties": false
        },
        "nfs": {
            "properties": {
                "type": { "enum": [ "nfs" ] },
                "remotePath": {
                    "type": "string",
                    "pattern": "^(/[^/]+)+$"
                },
                "server": {
                    "type": "string",
                    "oneOf": [
                        { "format": "host-name" },
                        { "format": "ipv4" },
                        { "format": "ipv6" }
                    ]
                }
            },
            "required": [ "type", "server", "remotePath" ],
            "additionalProperties": false
        },
        "tmpfs": {
            "properties": {
                "type": { "enum": [ "tmpfs" ] },
                "sizeInMB": {
                    "type": "integer",
                    "minimum": 16,
                    "maximum": 512
                }
            },
            "required": [ "type", "sizeInMB" ],
            "additionalProperties": false
        }
    }
}
```

[spdx-site]:
  https://spdx.org/licenses
[spdx-specification]:
  https://spdx.org/sites/spdx/files/SPDX-2.0.pdf
[json-schema]:
  http://json-schema.org/latest/json-schema-core.html

## Configuration

The configuration file is, by default, assumed to be `/etc/mcpkg.json`. If the
`-c <path>` or `--config-file=<path>` option is given on the command line, this
will be overridden by the given `<path>`.

The contents of the configuration file are as follows:

FIXME: Finish specifying configuration file.

```json
{
  "test": "lol"
}
```

The configuration file will be checked against a JSON schema.

FIXME: Add the JSON schema for the configuration file.

## Startup sequence

The server will do the following on startup, or whenever a reload is triggered:

- Check the command-line arguments for options.
  Based on these options, find the configuration file.
- Read and parse the configuration file.
- Based on the information in the configuration file, find the database. If the
  database format is SQLite 3 (we will support SQLite 3 and PostgreSQL), and 
  the database file does not yet exist, one will be created.
- Also in the configuration file is a path to a folder full of packages. Each
  package is represented by a `<sha256-hash>-<package-name>.json` file. Upon
  startup each package file is SHA256 hashed and compared against the database
  --- if the hash exists (as a primary key in the database), no change is made,
  but if it does not yet exist, we parse the package file and add it to the
  database (or edit it in the database if one with the same name already
  exists).
- Once the database is populated with packages, we check the cache directory
  (also specified in the configuration file) for folders with paths as follows:
  `CACHE_DIR/git-cache/<sha256-hash>-<package-name>.git/`. We also check if
  each folder is a Git repository, and if not, delete it. Any folders inside
  `CACHE_DIR/git-cache/` that do not follow that folder naming scheme are also
  deleted.
- The remaining Git repositories are updated by running `git pull`. If there
  are packages in the database that do not have corresponding Git caches, we
  download them.
- For each Git repository, we find all the Git tags and use them to update the
  list of available versions for a package in the database.
- In a separate table in the database a mapping exists between versioned
  packages and their Nix store paths. We check for existence of all Nix store
  paths and remove any that no longer exist.
- We then generate Nix expressions for each tagged version of a package, and
  build them with `nix-build` or similar. The resultant Nix store paths are
  added to the aforementioned table.
- Each Nix store path is then converted to a subsection of an uncompressed Zip
  archive (i.e.: a Zip archive without a directory at the end) and stored in 
  `CACHE_DIR/zip-cache/<sha256-hash>.zc`.
- We then run `ipfs add` on each file.

These updates should run once an hour or so.

This server also runs a small HTTPS webserver with the following API:

### `update`

Triggers an update.

### `status`

Returns some status information, including the amount of time since the last
update, and statistics about the number of packages etc.

### `dump-packages`

Dumps a JSON object whose keys are `<sha256-hash>-` of all the 

# Web server

Separately from this process, we maintain a webserver with the following API:

## FIXME

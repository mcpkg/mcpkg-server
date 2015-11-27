{ input }:

with import <nixpkgs> {};

let
  packageList = (builtins.fromJSON (builtins.readFile input)).mods;
  buildMCMod = { name, repo, rev, sha256, version, outpath } @ args: stdenv.mkDerivation {
    name = "${name}-${version}";
    buildInputs = [ gradle strace ];
    src = fetchgit {
      inherit rev sha256;
      url = repo;
    };
    buildPhase = ''
      export GRADLE_USER_HOME=$NIX_BUILD_TOP
      gradle build --stacktrace --offline
    '';
    installPhase = ''
      mkdir $out
      pwd
      ls
      cp -vir ${outpath}/* $out/
    '';
  };
in
rec {

  everything = builtins.listToAttrs (map (mod: {
    name = mod.name;
    value = let
      f = info: buildMCMod { inherit (mod) name repo; inherit (info) rev sha256 version; outpath = mod.out; };
    in
      builtins.listToAttrs (map (x: { name = "${x.version}"; value = f x; }) mod.versions);
  }) packageList);
}

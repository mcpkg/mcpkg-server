/*
 * Copyright © 2015 Shea Levy
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.nixos.mvn2nix;

import java.util.*;
import java.io.*;
import java.net.*;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.*;
import org.apache.maven.repository.internal.*;
import org.apache.maven.model.*;

import org.eclipse.aether.*;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.graph.*;
import org.eclipse.aether.artifact.*;
import org.eclipse.aether.metadata.*;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.repository.*;
import org.eclipse.aether.spi.connector.layout.*;
import org.eclipse.aether.spi.connector.transport.*;
import org.eclipse.aether.transfer.*;

/**
 * A Mojo to generate JSON for use with Nix's Maven repository generation
 * functions
 *
 * @author Shea Levy
 */
@Mojo(name = "mvn2nix")
public class Mvn2NixMojo extends AbstractMojo {
    @Component
    private MavenProject project;

    @Component
    private RepositorySystem repoSystem;

    @Component
    private RepositoryLayoutProvider layoutProvider;

    @Component
    private TransporterProvider transporterProvider;

    @Parameter(property="repositorySystemSession", readonly=true)
    private DefaultRepositorySystemSession repoSession;

    @Parameter(property="mvn2nixOutputFile",
        defaultValue="project-info.json")
    private String outputFile;

    fun fromMavenExclusion(excl: org.apache.maven.model.Exclusion): Exclusion {
        return Exclusion(excl.getGroupId(), excl.getArtifactId(), null, null);
    }

    fun fromMavenDependency(dep: org.apache.maven.model.Dependency): Dependency {
        val art = DefaultArtifact(dep.getGroupId(),
                                  dep.getArtifactId(),
                                  dep.getClassifier(),
                                  dep.getType(),
                                  dep.getVersion());
        val excls = HashSet<Exclusion>();
        for(excl in dep.getExclusions()) {
            excls.add(fromMavenExclusion(excl));
        }
        return Dependency(art, dep.getScope(),
                          Boolean(dep.isOptional()), excls);
    }

    fun fromMavenArtifact(art: org.apache.maven.artifact.Artifact): Artifact {
        return DefaultArtifact(art.getGroupId(),
                               art.getArtifactId(),
                               art.getClassifier(),
                               art.getType(),
                               art.getVersion());
    }

    fun emitArtifactBody(art:  Artifact,
                         deps: Collection<Dependency>,
                         gen:  JsonGenerator): Unit {
        gen.write("artifactId", art.getArtifactId());
        gen.write("groupId",    art.getGroupId());
        gen.write("version",    art.getVersion());
        gen.write("classifier", art.getClassifier());
        gen.write("extension",  art.getExtension());
        if(deps != null) {
            gen.writeStartArray("dependencies");
            for(dep in deps) {
                gen.writeStartObject();

                emitArtifactBody(dep.getArtifact(), null, gen);

                gen.write("scope", dep.getScope());
                gen.write("optional", dep.isOptional());

                gen.writeStartArray("exclusions");
                for(excl in dep.getExclusions()) {
                    gen.writeStartObject();
                    gen.write("artifactId", excl.getArtifactId());
                    gen.write("classifier", excl.getClassifier());
                    gen.write("extension",  excl.getExtension());
                    gen.write("groupId",    excl.getGroupId());
                    gen.writeEnd();
                }
                gen.writeEnd();

                gen.writeEnd();
            }
            gen.writeEnd();
        }
    }

    data class ArtifactDownloadInfo(var url: String = "",
                                    var hash: String = "");

    fun getDownloadInfoImpl(base:        String,
                            fileLoc:     URI,
                            checksums:   List<RepositoryLayout.Checksum>,
                            description: String,
                            transport:   Transporter): ArtifactDownloadInfo {
        val abs: URI;
        try {
            abs = URI(base + "/" + fileLoc);
        } catch(URISyntaxException e) {
            throw MojoExecutionException("Parsing repository URI", e);
        }

        val res = ArtifactDownloadInfo();
        res.url = abs.toString();

        var task: GetTask;
        for(ck in checksums) {
            if(ck.getAlgorithm().equals("SHA-1")) {
                task = GetTask(ck.getLocation());
                break;
            }
        }

        if(task == null) {
            throw MojoExecutionException("No SHA-1 for ${desc}");
        }

        try {
            transport.get(task);
        } catch(Exception e) {
            throw MojoExecutionException("Downloading SHA-1 for ${desc}", e);
        }

        try {
            res.hash = String(task.getDataBytes(), 0, 40, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            throw MojoExecutionException("Your JVM doesn't support UTF-8", e);
        }
        return res;
    }

    fun getDownloadInfo(art:       Artifact,
                        layout:    RepositoryLayout,
                        base:      String,
                        transport: Transporter): ArtifactDownloadInfo {
        val fileLoc = layout.getLocation(art, false);
        val checksums = layout.getChecksums(art, false, fileLoc);
        return getDownloadInfoImpl(base, fileLoc, checksums,
                                   art.toString(), transport);
    }

    fun getDownloadInfo(meta:      Metadata,
                        layout:    RepositoryLayout,
                        base:      String,
                        transport: Transporter): ArtifactDownloadInfo {
        val fileLoc = layout.getLocation(meta, false);
        val checksums = layout.getChecksums(meta, false, fileLoc);
        return getDownloadInfoImpl(base, fileLoc, checksums,
                                   meta.toString(), transport);
    }

    private void handleDependency(Dependency dep,
        List<RemoteRepository> repos,
        Set<Dependency> work,
        Set<Artifact> printed,
        JsonGenerator gen) throws MojoExecutionException {
        Artifact art = dep.getArtifact();

        ArtifactDownloadInfo metadataInfo = null;
        String unresolvedVersion = art.getVersion();
        if(art.isSnapshot()) {
            val vReq = VersionRequest(art, repos, null);
            val res;
            try {
                res = repoSystem.resolveVersion(repoSession, vReq);
            } catch(VersionResolutionException e) {
                throw MojoExecutionException("Resolving version of ${art}", e);
            }
            if(!res.getVersion().equals(art.getVersion())) {
                art = DefaultArtifact(art.getGroupId(),
                                      art.getArtifactId(),
                                      art.getClassifier(),
                                      art.getExtension(),
                                      res.getVersion());
                if(res.getRepository() is RemoteRepository) {
                    val repo = res.getRepository() as? RemoteRepository;
                    val metadata
                        = DefaultMetadata(art.getGroupId(),
                                          art.getArtifactId(),
                                          unresolvedVersion,
                                          "maven-metadata.xml",
                                          Metadata.Nature.RELEASE_OR_SNAPSHOT);
                    RepositoryLayout layout;
                    try {
                        layout = layoutProvider.newRepositoryLayout(repoSession,
                                                                    repo);
                    } catch(NoRepositoryLayoutException e) {
                        throw MojoExecutionException("Getting repository layout", e);
                    }
                    String base = repo.getUrl();
                    /* TODO: Open the transporters all at
                     * once */
                    transporterProvider.newTransporter(repoSession, repo).use {
                        metadataInfo = getDownloadInfo(metadata, layout, base, it);
                    }
                }
            }
        }
        val req = ArtifactDescriptorRequest(art, repos, null);
        val res: ArtifactDescriptorResult;
        try {
            res = repoSystem.readArtifactDescriptor(repoSession, req);
        } catch(ArtifactDescriptorException e) {
            throw MojoExecutionException("getting descriptor for ${art}", e);
        }

        /* Ensure we're keying on the things we care about */
        Artifact artKey = DefaultArtifact(art.getGroupId(),
            art.getArtifactId(),
            art.getClassifier(),
            art.getExtension(),
            unresolvedVersion);
        if(printed.add(artKey)) {
            gen.writeStartObject();
            emitArtifactBody(art, res.getDependencies(), gen);
            if(metadataInfo != null) {
                gen.write("unresolved-version", unresolvedVersion);
                gen.write("repository-id", res.getRepository().getId());
                gen.writeStartObject("metadata");
                gen.write("url", metadataInfo.url);
                gen.write("sha1", metadataInfo.hash);
                gen.writeEnd();
            }
            if(res.getRepository() is RemoteRepository) {
                RemoteRepository repo = (RemoteRepository) res
                    .getRepository();
                gen.write("authenticated",
                    repo.getAuthentication() != null);
                RepositoryLayout layout;
                try {
                    layout = layoutProvider.newRepositoryLayout(repoSession,
                                                                repo);
                } catch(NoRepositoryLayoutException e) {
                    throw MojoExecutionException("Getting repository layout", e);
                }

                String base = repo.getUrl();
                /* TODO: Open the transporters all at once */
                transporterProvider.newTransporter(repoSession, repo).use {
                    val info = getDownloadInfo(art, layout, base, it);
                    gen.write("url", info.url);
                    gen.write("sha1", info.hash);

                    gen.writeStartArray("relocations");
                    for(rel in res.getRelocations()) {
                        Artifact relPom = DefaultArtifact(rel.getGroupId(),
                                                          rel.getArtifactId(),
                                                          rel.getClassifier(),
                                                          "pom",
                                                          rel.getVersion());
                        gen.writeStartObject();
                        info = getDownloadInfo(art, layout, base, it);
                        gen.write("url", info.url);
                        gen.write("sha1", info.hash);
                        gen.writeEnd();
                    }
                    gen.writeEnd();
                }
            }
            gen.writeEnd();
        }

        if(!art.getExtension().equals("pom")) {
            val pomArt = DefaultArtifact(art.getGroupId(), art.getArtifactId(),
                                         null, "pom", unresolvedVersion);
            val pomDep = Dependency(pomArt, "compile",
                                    Boolean(false),
                                    dep.getExclusions());
            work.add(pomDep);
        }

        for(subDep in res.getDependencies()) {
            if(subDep.isOptional()) {
                continue;
            }
            String scope = subDep.getScope();
            if(scope != null && (scope.equals("provided")
                || scope.equals("test")
                || scope.equals("system"))) {
                continue;
            }
            Artifact subArt = subDep.getArtifact();
            val excls = HashSet<Exclusion>();
            boolean excluded = false;
            for(excl in dep.getExclusions()) {
                if(excl.getArtifactId().equals(subArt.getArtifactId()) &&
                   excl.getGroupId().equals(subArt.getGroupId())) {
                    excluded = true;
                    break;
                }
                excls.add(excl);
            }
            if(excluded) { continue; }
            for(excl in subDep.getExclusions()) { excls.add(excl); }

            Dependency newDep = Dependency(subArt, dep.getScope(),
                                           dep.getOptional(), excls);
            work.add(newDep);
        }
    }

    @Override
    fun execute(): Unit {
        repoSession = DefaultRepositorySystemSession(repoSession);
        val d = ParentPOMPropagatingArtifactDescriptorReaderDelegate();
        repoSession.setConfigProperty(
            ArtifactDescriptorReaderDelegate.class.getName(),
            d);
        repoSession.setReadOnly();

        val work = HashSet<Dependency>();
        val seen = HashSet<Dependency>();
        val printed = HashSet<Artifact>();
        for(p in project.getBuildPlugins()) {
            val art = DefaultArtifact(p.getGroupId(), p.getArtifactId(),
                                      null, "jar", p.getVersion());
            Dependency dep = Dependency(art, "compile");
            work.add(dep);
            for(subDep in p.getDependencies()) {
                work.add(fromMavenDependency(subDep));
            }
        }
        for(dep in project.getDependencies()) {
            work.add(fromMavenDependency(dep));
        }
        FileOutputStream(outputFile).use {
            val gen = Json.createGenerator(it);

            gen.writeStartObject();

            gen.writeStartObject("project");
            emitArtifactBody(fromMavenArtifact(project.getArtifact()),
                             work, gen);
            gen.writeEnd();

            gen.writeStartArray("dependencies");

            val repos = ArrayList<RemoteRepository>();
            repos.addAll(project.getRemoteProjectRepositories());
            repos.addAll(project.getRemotePluginRepositories());

            while(!work.isEmpty()) {
                Iterator<Dependency> it = work.iterator();
                Dependency dep = it.next();
                it.remove();

                if(seen.add(dep)) {
                    handleDependency(dep, repos, work,
                                     printed, gen);
                }
            }
            gen.writeEnd();

            gen.writeEnd();
        }
    }
}

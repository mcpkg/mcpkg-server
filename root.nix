{ input, deps }:

with import <nixpkgs> {};

let
    #repo = ./repo;
in
let
    settings = deps: let
      repo = mergeDeps { name = "repo"; inputs = deps; };
    in writeText "settings.xml" ''
      <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                            http://maven.apache.org/xsd/settings-1.0.0.xsd">
        <localRepository>${repo}/repository/</localRepository>
      </settings>
    '';
  packageList = (builtins.fromJSON (builtins.readFile input)).mods;
  versionList = fetchurl {
    url = "http://export.mcpbot.bspk.rs/versions.json";
    sha256 = "14xwc3zj7g63zv8f8yfp2qalycvsffzkajhh9frxndb7qdxp0spz";
  };
  # changes often
  forgeVersions = fetchurl {
    url = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/json";
    sha256 = "0drafbnadc0dflgja41x9nrspl19zz6qbbv0vzas5wfybdzaqz5w";
  };
  buildMCMod = { name, repo, rev, sha256, version, outpath, deps } @ args: stdenv.mkDerivation {
    name = "${name}-${version}";
    buildInputs = [ gradle strace ];
    src = fetchgit {
      inherit rev sha256;
      url = repo;
    };
    buildPhase = ''
      export GRADLE_USER_HOME=$NIX_BUILD_TOP/.gradle
      export M2_HOME=$NIX_BUILD_TOP/.m2
      mkdir -pv $M2_HOME/conf/
      mkdir -pv $GRADLE_USER_HOME/caches/minecraft/net/minecraft/minecraft/1.7.10/
      mkdir -pv $GRADLE_USER_HOME/caches/minecraft/net/minecraft/minecraft_server/1.7.10
      cp -vi ${./minecraft-1.7.10.jar} $GRADLE_USER_HOME/caches/minecraft/net/minecraft/minecraft/1.7.10/minecraft-1.7.10.jar
      cp -vi ${./minecraft-1.7.10.jar.md5} $GRADLE_USER_HOME/caches/minecraft/net/minecraft/minecraft/1.7.10/minecraft-1.7.10.jar.md5
      cp -vi ${./minecraft_server-1.7.10.jar} $GRADLE_USER_HOME/caches/minecraft/net/minecraft/minecraft_server/1.7.10/minecraft_server-1.7.10.jar
      cp -vi ${./minecraft_server-1.7.10.jar.md5} $GRADLE_USER_HOME/caches/minecraft/net/minecraft/minecraft_server/1.7.10/minecraft_server-1.7.10.jar.md5
      cp -vi ${settings deps} $M2_HOME/conf/settings.xml
      cp ${versionList} $GRADLE_USER_HOME/caches/minecraft/McpMappings.json
      cp ${forgeVersions} $GRADLE_USER_HOME/caches/minecraft/forgeVersions.json
      env | grep m2
      #strace -ff -o $NIX_BUILD_TOP/gradle.trace
      gradle build --no-daemon --offline #|| egrep 'forge.*ENOENT' $NIX_BUILD_TOP/gradle.trace.* --color=always
    '';
    installPhase = ''
      mkdir $out
      pwd
      cp -vir ${outpath}/* $out/
    '';
  };
  fetchMavenObject = artifact: stdenv.mkDerivation {
    name = lib.replaceChars [":"] ["."] artifact;
    buildInputs = [ maven strace ];
    unpackPhase = "true";
    buildPhase = ''
      export M2_HOME=$NIX_BUILD_TOP/.m2/
      strace -e open,stat -f mvn org.apache.maven.plugins:maven-dependency-plugin:2.1:get -DrepoUrl=https://repo1.maven.org/maven2 -Dartifact=${artifact}
    '';
  };
  fetchSingleDep = { groupId, artifactId, v, sha256, timestamp, deps, chosen ? "", suffix ? "" }: stdenv.mkDerivation {
    name = lib.replaceChars ["[" "," ")"] ["" "-" ""] "${groupId}-${artifactId}-${v}";
    buildInputs = [ curl ];
    mirrors = [
      "http://repo1.maven.org/maven2"
      "http://files.minecraftforge.net/maven"
      "https://libraries.minecraft.net"
      "http://chickenbones.net/maven"
      "http://klingon.angeldsis.com/maven"
      "http://maven.k-4u.nl"
      "http://maven.cil.li"
      "http://maven.ic2.player.to"
      "http://mvn.rx14.co.uk/repo"
      "http://dl.tsr.me/artifactory/libs-release-local"
    ];
    DIR1 = "${lib.replaceChars ["."] ["/"] groupId}/${artifactId}" + (if chosen == "" then "/${v}" else "");
    DIR2 = "${lib.replaceChars ["."] ["/"] groupId}/${artifactId}" + (if chosen == "" then "/${v}" else "/${chosen}");
    filename = 
      (if chosen == "" then
        (if timestamp == null then "${artifactId}-${v}" else "${artifactId}-${lib.replaceStrings ["SNAPSHOT"] [timestamp] v}")
      else
        ("${artifactId}-${chosen}")) + suffix;
    inherit timestamp;
    unpackPhase = ":";
    buildPhase = builtins.readFile ./dep-fetcher.sh;
    dontInstall = true;
    outputHashAlgo = "sha256";
    outputHashMode = "recursive";
    outputHash = sha256;
  } // { inherit deps; };
  getDeps = pkg: let
    topLevel = pkg.deps;
    secondLevel = lib.concatLists (map getDeps topLevel);
  in
    if pkg.deps == [] then [] else builtins.trace "reading deps: ${pkg.name}" (topLevel ++ secondLevel);
  mergeDeps = { name, inputs }: buildEnv {
    inherit name;
    paths = inputs ++ lib.concatLists (map (pkg: getDeps pkg) inputs);
  };
in
rec {

  everything = builtins.listToAttrs (map (mod: {
    name = mod.name;
    value = let
      f = info: buildMCMod {
        inherit (mod) name repo; inherit (info) rev sha256 version; outpath = mod.out;
        deps = [
dependencies."com.googlecode.javaewah:JavaEWAH"."0.5.6"
dependencies."net.minecraftforge.gradle:ForgeGradle"."1.2-SNAPSHOT"
dependencies."net.sf.opencsv:opencsv"."2.3"
dependencies."org.sonatype.oss:oss-parent"."7"
dependencies."org.sonatype.oss:oss-parent"."9"
dependencies."com.github.jponge:lzma-java"."1.3"
dependencies."com.github.abrarsyed.jastyle:jAstyle"."1.2"
dependencies."net.sf.trove4j:trove4j"."2.1.0"
dependencies."com.cloudbees:diff4j"."1.1"
dependencies."com.cloudbees:cloudbees-oss-parent"."1"
dependencies."net.md-5:SpecialSource"."1.7.3"
dependencies."com.github.tony19:named-regexp"."0.2.3"
dependencies."org.ow2.asm:asm-debug-all"."5.0.3"
dependencies."org.ow2.asm:asm-parent"."5.0.3"
dependencies."org.ow2:ow2"."1.3"
dependencies."com.nothome:javaxdelta"."2.0.1"
dependencies."net.minecraftforge:forge"."1.7.10-10.13.4.1448-1.7.10"
dependencies."tv.twitch:twitch"."5.16"
dependencies."codechicken:ForgeMultipart"."1.7.10-1.2.0.345"
dependencies."codechicken:CodeChickenLib"."1.7.10-1.1.3.138"
dependencies."codechicken:CodeChickenCore"."1.7.10-1.0.7.47"
dependencies."codechicken:NotEnoughItems"."1.7.10-1.0.5.111"
dependencies."com.mod-buildcraft:buildcraft"."7.0.9"
dependencies."pneumaticCraft:PneumaticCraft-1.7.10"."1.9.15-105"
dependencies."li.cil.oc:OpenComputers"."MC1.7.10-1.5.12.26"
dependencies."net.industrial-craft:industrialcraft-2"."2.2.717-experimental"
dependencies."net.mcft.copy.betterstorage:BetterStorage"."1.7.10-0.11.3.123.20"
dependencies."appeng:Waila"."1.5.10_1.7.10"
dependencies."org.lwjgl.lwjgl:lwjgl_util"."2.9.1"
];
      };
    in
      builtins.listToAttrs (map (x: { name = "${x.version}"; value = f x; }) mod.versions);
  }) packageList);
  inherit mergeDeps getDeps;
  dependencies = builtins.listToAttrs (map (dep: {
      name = "${dep.groupId}:${dep.artifactId}";
      value = builtins.listToAttrs (map (ver: { name = ver.v; value = fetchSingleDep (ver // {
          inherit (dep) groupId artifactId;
          #version = ver.v; 
          sha256 = if ver.sha256 == "" then "1gwydc5wdm74gaq00b8p7lxs1ck6ry336lmxz0wzjkya2w2fb2ay" else ver.sha256;
          timestamp = if ver ? timestamp then ver.timestamp else null;
          deps = if ver ? deps then ver.deps else [];
        }); }) dep.versions);
    }) (deps dependencies));
}

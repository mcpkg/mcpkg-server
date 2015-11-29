{ input }:

with import <nixpkgs> {};

let
    repo = ./repo;
    settings = writeText "settings.xml" ''
      <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                            http://maven.apache.org/xsd/settings-1.0.0.xsd">
        <localRepository>${repo}</localRepository>
      </settings>
    '';
in
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
      export GRADLE_USER_HOME=$NIX_BUILD_TOP/.gradle
      export M2_HOME=$NIX_BUILD_TOP/.m2
      mkdir -pv $M2_HOME/conf/
      mkdir -pv $GRADLE_USER_HOME/caches/minecraft/net/minecraft/minecraft/1.7.10/
      mkdir -pv $GRADLE_USER_HOME/caches/minecraft/net/minecraft/minecraft_server/1.7.10
      cp -vi ${./minecraft-1.7.10.jar} $GRADLE_USER_HOME/caches/minecraft/net/minecraft/minecraft/1.7.10/minecraft-1.7.10.jar
      cp -vi ${./minecraft-1.7.10.jar.md5} $GRADLE_USER_HOME/caches/minecraft/net/minecraft/minecraft/1.7.10/minecraft-1.7.10.jar.md5
      cp -vi ${./minecraft_server-1.7.10.jar} $GRADLE_USER_HOME/caches/minecraft/net/minecraft/minecraft_server/1.7.10/minecraft_server-1.7.10.jar
      cp -vi ${./minecraft_server-1.7.10.jar.md5} $GRADLE_USER_HOME/caches/minecraft/net/minecraft/minecraft_server/1.7.10/minecraft_server-1.7.10.jar.md5
      cp -vi ${settings} $M2_HOME/conf/settings.xml
      env | grep m2
      strace -ff -o gradle.trace gradle build --no-daemon || egrep 'repo.*ENOENT' gradle.trace.*
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
in
rec {

  everything = builtins.listToAttrs (map (mod: {
    name = mod.name;
    value = let
      f = info: buildMCMod { inherit (mod) name repo; inherit (info) rev sha256 version; outpath = mod.out; };
    in
      builtins.listToAttrs (map (x: { name = "${x.version}"; value = f x; }) mod.versions);
  }) packageList);
  test = fetchMavenObject "org.eclipse.core:resources:3.2.1-R32x_v20060914";
}

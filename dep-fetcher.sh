mkdir -pv $out/repository/{$DIR1,$DIR2}
function tryfetch {
  if [ ! -e "$2" ] ; then
    echo trying "$1"
    curl -fk "$1" > "$2" || (echo failed; rm "$2")
  else
    true
  fi
}
for mirror in $mirrors; do
  tryfetch ${mirror}/${DIR2}/${filename}.pom $out/repository/$DIR2/${filename}.pom
  tryfetch ${mirror}/${DIR2}/${filename}.jar $out/repository/$DIR2/${filename}.jar
  tryfetch ${mirror}/${DIR1}/maven-metadata.xml $out/repository/$DIR1/maven-metadata.xml
done
find $out -type f

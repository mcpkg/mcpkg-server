let
  deps = self: [
    { groupId = "com.googlecode.javaewah"; artifactId = "JavaEWAH"; versions = [ {v="0.5.6";sha256="03sl0izv83pvc3yfjayllgk9xy52wlpkmxan66sj3qa583g1p6cz"; deps = [
      self."org.sonatype.oss:oss-parent"."5"
    ]; } ]; }
    { groupId = "net.minecraftforge.gradle"; artifactId = "ForgeGradle"; versions = [
      {v="1.2-SNAPSHOT";timestamp="20150805.153118-302";sha256="0dlbpy0cn9lllx7xg45lsqs3cbjbkisa18p77w1ss3jfxbgyv8yk"; deps = [
        self."net.minecraftforge.srg2source:Srg2Source"."3.2-SNAPSHOT"
        self."org.apache.httpcomponents:httpclient"."4.3.3"
        self."com.google.code.gson:gson"."2.2.4"
        self."com.google.guava:guava"."18.0"
        self."org.apache.httpcomponents:httpmime"."4.3.3"
        self."de.oceanlabs.mcp:mcinjector"."3.2-SNAPSHOT"
        self."de.oceanlabs.mcp:RetroGuard"."3.6.6"
        self."org.eclipse.jdt:org.eclipse.jdt.core"."3.10.0.v20131029-1755"
      ]; }
    ]; }
    { groupId = "de.oceanlabs.mcp"; artifactId = "RetroGuard"; versions = [ { v="3.6.6"; sha256="1shxy207v3q70g46z5sgn1x3m9lm6prfqrlh363a2s0794r0wwl9"; deps = [
      self."net.sf.jopt-simple:jopt-simple"."4.7"
    ];} ]; }
    { groupId = "net.sf.opencsv"; artifactId = "opencsv"; versions = [ { v="2.3"; sha256="1fqp7cqzzw8fjk4iznlgqn7kd4zgnszsc9h5rq6gkyadj8334mdw"; } ]; }
    { groupId = "org.sonatype.oss"; artifactId = "oss-parent"; versions = [
      { v="5"; sha256="14k59b6sqnyc0rpdphqih59xcs42hwkrr8x9sdlsrag9hbzh31d5"; }
      { v="6"; sha256="020c094kapn4gdl8sdp04xypvyi881xrlk5z32dylwh9mbnskxyv"; }
      { v="7"; sha256="0lrb7c0lp4svrzn2qxf88pf9clqcynjkzr9ksll51jns1340zhql"; }
      { v="9"; sha256="17875s32p2mpa5r66g0nlazb7x8fs6x5mnwg8ddd08d9pfp8378z"; }
    ]; }
    { groupId = "com.github.jponge"; artifactId = "lzma-java"; versions = [ { v="1.3"; sha256="06s47n6z68zd36w9n5wh6gxrpy794997p27inmpf6i2kk4rl5gjb"; } ]; }
    { groupId = "com.github.abrarsyed.jastyle"; artifactId = "jAstyle"; versions = [ { v="1.2"; sha256="1gwydc5wdm74gaq00b8p7lxs1ck6ry336lmxz0wzjkya2w2fb2ay"; } ]; }
    { groupId = "net.sf.trove4j"; artifactId = "trove4j"; versions = [ { v="2.1.0"; sha256="1xfrdd5vmx1anccpjsszsl194gcqnma4a1mzrrmzrg1iqmlcgh2l"; } ]; }
    { groupId = "com.cloudbees"; artifactId = "diff4j"; versions = [ { v="1.1"; sha256="05c5cd0kp7dfi3m8b7wd8hv0a9cmhzf3v989gy076mdnp62lgc28"; deps = [
      self."org.jvnet.localizer:localizer"."1.12"
      self."commons-io:commons-io"."1.4"
    ];} ]; }
    { groupId = "com.cloudbees"; artifactId = "cloudbees-oss-parent"; versions = [ { v="1"; sha256="18hcd55nlqhy17iy0rzha0zygi7shp3am8pva1zpd8x2pwj73rzc"; } ]; }
    { groupId = "net.md-5"; artifactId = "SpecialSource"; versions = [ { v="1.7.3"; sha256="04nymiwi3wnqfcaqwwh507ydbkpgagfd365lcgpjqim0npng03w1"; } ]; }
    { groupId = "com.github.tony19"; artifactId = "named-regexp"; versions = [ { v="0.2.3"; sha256="11r3bw643gjclpg7vm5wgf6a04dzc6jnr8n9x4d3rk5h8k45m4pr"; } ]; }
    { groupId = "org.ow2.asm"; artifactId = "asm-debug-all"; versions = [ { v="5.0.3"; sha256="15miw6g8lxq3mgxlpsz77bwkfcnpggvcakrw6kv7wb43bcvhv2w4"; } ]; }
    { groupId = "org.ow2.asm"; artifactId = "asm-parent"; versions = [ { v="5.0.3"; sha256="1kqf13h4wwvhyfx36hqcacmnszsnaa3nxdpic9lkd8dvnn0p4dhf"; } ]; }
    { groupId = "org.ow2"; artifactId = "ow2"; versions = [ { v="1.3"; sha256="14v5gjf03sh2a9gj3lfl6y80g7hif4v68vz607bv2ixjsafnzpas"; } ]; }
    { groupId = "com.nothome"; artifactId = "javaxdelta"; versions = [ { v="2.0.1"; sha256="1yjf13sk4b4q4jp9zmh0gkzrf24qgcvrhrd7mzkpvmzif6bbic9p"; deps = [
      self."trove:trove"."1.0.2"
    ]; } ]; }
    { groupId = "net.minecraftforge.srg2source"; artifactId = "Srg2Source"; versions = [ { v="3.2-SNAPSHOT"; timestamp="20150109.190932-47"; sha256="0ygrk1yvygj3p3djpgr8phzsirc03a125nqhgmfvvgmgv6dryzjs"; deps = [
      self."org.eclipse.core:jobs"."3.5.300-v20130429-1813"
      self."org.eclipse:osgi"."3.9.1-v20130814-1242"
      self."org.eclipse.core:contenttype"."3.4.200-v20130326-1255"
      self."org.eclipse.core:resources"."3.2.1-R32x_v20060914"
      self."org.eclipse.equinox:common"."3.6.200-v20130402-1505"
      self."org.eclipse.text:org.eclipse.text"."3.5.101"
      self."org.eclipse.jgit:org.eclipse.jgit"."3.2.0.201312181205-r"
      self."org.eclipse.equinox:preferences"."3.5.100-v20130422-1538"
      self."org.eclipse.core:runtime"."3.9.0-v20130326-1255"
    ];} ]; }
    { groupId = "org.apache.httpcomponents"; artifactId = "httpclient"; versions = [ { v="4.3.3"; sha256="0j4hy47pm6pfnzp6zr75izd1590y3fhi5r8k53xbi3qy45nf5qh3"; deps = [
      self."org.apache.httpcomponents:httpcomponents-client"."4.3.3"
      self."org.apache.httpcomponents:httpcore"."4.3.2"
      self."commons-logging:commons-logging"."1.1.3"
      self."commons-codec:commons-codec"."1.6"
      self."commons-codec:commons-codec"."1.9"
    ]; } ]; }
    { groupId = "org.apache.httpcomponents"; artifactId = "httpcomponents-client"; versions = [ { v="4.3.3"; sha256="04273bfvlxbim5y51hxaf388yjblvyj4bmvgv6287m817gk0xc3h"; deps = [
      self."org.apache.httpcomponents:project"."7"
    ]; } ]; }
    { groupId = "org.apache.httpcomponents"; artifactId = "project"; versions = [ { v="7"; sha256="0j23y5b6afzpclqns6rijy68cc5jxmgkaip8bd9a4wv1a8yy0wzj"; deps = [
      self."org.apache:apache"."13"
    ];} ]; }
    { groupId = "org.apache"; artifactId = "apache"; versions = [
      { v="4"; sha256="1swb4bnf8br2iji7c1q7v48n59dyc5nj3rvvjmpyxq4mndwc1zgs"; }
      { v="9"; sha256="1c4mzw31bb2d2m5xz40nbgzm9q92pk4xrc3dvvg6gq56lw1l3gvh"; }
      { v="13"; sha256="1714gy6cwvv6cbni1n1z2lbbp8r9vrhsl63mrl913z3qg2nrsfyi"; }
    ]; }
    { groupId = "com.google.code.gson"; artifactId = "gson"; versions = [ { v="2.2.4"; sha256="1v7z86n3vx8gky31axjyqxmd6afzdsimdj5pdnpbrk7yi6827xy0"; } ]; }
    { groupId = "com.google.guava"; artifactId = "guava"; versions = [ { v="18.0"; sha256="05i4nlz4p8i9rmdbz4pirj70ic3hkvmf8p2smvg93qfpvjlp5ms0"; deps = [
      self."com.google.guava:guava-parent"."18.0"
    ]; } ]; }
    { groupId = "com.google.guava"; artifactId = "guava-parent"; versions = [ { v="18.0"; sha256="013a79kl0f8c4givzhmqwhl2rdk9mjglmsmxi67lp3cavrnrkyyv"; } ]; }
    { groupId = "org.apache.httpcomponents"; artifactId = "httpmime"; versions = [ { v="4.3.3"; sha256="06fliq8z54277ng0lrzqjzhid0i5pq4jypqadfk6kbjggmrcfj0d"; } ]; }
    { groupId = "de.oceanlabs.mcp"; artifactId = "mcinjector"; versions = [ { v="3.2-SNAPSHOT"; timestamp = "20150605.000822-18"; sha256="00f902g61gyik8izv3c04g1c30ps6r97ljvlv4607hc4pv3mjv0x"; } ]; }
    { groupId = "net.sf.jopt-simple"; artifactId = "jopt-simple"; versions = [ { v="4.7"; sha256="1zm4asrig066g5m3l1yikm94a3r06g2yh1vdlywdaj35rbh3hf6w"; } ]; }
    { groupId = "org.jvnet.localizer"; artifactId = "localizer"; versions = [ { v="1.12"; sha256="1rjfp0np0p3a2dw8vianm2wvadi1sy5m4yjv0m9nci3bxmqw3d59"; deps = [
      self."org.jvnet.localizer:localizer-parent"."1.12"
    ]; } ]; }
    { groupId = "org.jvnet.localizer"; artifactId = "localizer-parent"; versions = [ { v="1.12"; sha256="1arbp3kgbcwnpsx0hi888lrwgk6k4s3n2hrnhg711mh08gbys4c0"; } ]; }
    { groupId = "commons-io"; artifactId = "commons-io"; versions = [ { v="1.4"; sha256="0q8dr8xdm71q1kjna07wvwmyr1g3r2dfcfnriyyy50pmdqrcj7l0"; deps = [
      self."org.apache.commons:commons-parent"."7"
    ]; } ]; }
    { groupId = "org.apache.commons"; artifactId = "commons-parent"; versions = [
      { v="7"; sha256="1k0gk49bxm2gglf9dm1r4gdacf8v6h94cnwndq8hc6qmgvd6yfrb"; deps = [ self."org.apache:apache"."4" ]; }
      { v="22"; sha256="1c51kmbvm9prn8x9l88ms489qv8wriwn48v4djd5715q64n904r8"; deps = [ self."org.apache:apache"."9" ]; }
      { v="28"; sha256="0r7nkvfim3p399mwp9p74k0ncirxin8m8ynw2lx37g21wn332ba7"; }
      { v="32"; sha256="0xz8rgpqwyg823ck8m2vp42kbsf7abyg8faahlxg7s2307w54sa0"; }
    ]; }
    { groupId = "trove"; artifactId = "trove"; versions = [ { v="1.0.2"; sha256="1pcw1xin75i5amldwvlcvv4cy56y5drgjd43662yr7nmigbcxbdj"; } ]; }
    { groupId = "org.eclipse.jdt"; artifactId = "org.eclipse.jdt.core"; versions = [ { v="3.10.0.v20131029-1755"; sha256="1f0gw3idw0p6mwimag26yhaksmrgz7bgnig1mw23qr2kg1rly0f7"; } ]; }
    { groupId = "org.eclipse.core"; artifactId = "jobs"; versions = [ { v="3.5.300-v20130429-1813"; sha256="1lqjh4i6qbcvdph781y9v2cspw8c9ln7dgncnwb5apv7kylkxc2z"; } ]; }
    { groupId = "org.eclipse"; artifactId = "osgi"; versions = [ { v="3.9.1-v20130814-1242"; sha256="1w704hvgii0simghchdan35qbq1hq3qbmpxqhdpm38pmx2bcav0r"; } ]; }
    { groupId = "org.eclipse.core"; artifactId = "contenttype"; versions = [ { v="3.4.200-v20130326-1255"; sha256="1r8lsqflr5hxnqkkmin38mpr5r5d2fy1fqdsaay0xl60na4hg9vh"; deps = [
      self."org.eclipse.equinox:registry"."[3.2.0,4.0.0)"
    ]; } ]; }
    { groupId = "org.eclipse.core"; artifactId = "runtime"; versions = [ { v="3.9.0-v20130326-1255"; sha256="1lnxjy9xm8fic2df1sq34j6m585laf0kqfiln2834cvy0m3bvp8b"; deps = [
      self."org.eclipse.equinox:app"."[1.0.0,2.0.0)"
    ]; } ]; }
    { groupId = "org.eclipse.core"; artifactId = "expressions"; versions = [ { v="[3.1.0,4.0.0)"; sha256="1xfxz3x1dil2rmcx35iavj4snxscav28ibmcq5ib665smkysqawl"; chosen = "3.3.0-v20070606-0010"; } ]; }
    { groupId = "org.eclipse.core"; artifactId = "filesystem"; versions = [ { v="[1.0.0,2.0.0)"; sha256="19vpf9r51zp04s3gvbl6jq8741gc32bmwg0zdv40wn7z18v99vhk"; chosen = "1.1.0-v20070606"; } ]; }
    { groupId = "org.eclipse.core"; artifactId = "org.eclipse.core.commands"; versions = [ { v="3.6.0"; sha256="0g69ns22vz74k22p788xjk3k4ffjxk9sl7p264bf899g8ngdx2da"; } ]; }
    { groupId = "org.eclipse.core"; artifactId = "resources"; versions = [ { v="3.2.1-R32x_v20060914"; sha256="1xrv9pfkpkiqfl91fvppza0phj58hy03czzkw27m5lgnn15g0wfr"; deps = [
      self."org.eclipse.core.runtime:compatibility"."[3.1.0,4.0.0)"
      self."org.eclipse.core:expressions"."[3.1.0,4.0.0)"
      self."org.eclipse.core:filesystem"."[1.0.0,2.0.0)"
    ]; } ]; }
    { groupId = "org.eclipse.core.runtime"; artifactId = "compatibility"; versions = [ { v="[3.1.0,4.0.0)"; sha256="01izkyrqjpfrpgcndma4pccnn2n1m87icksz5xfp4wxj2yp376as"; chosen = "3.1.200-v20070502"; deps = [
      self."org.eclipse.update:configurator"."[3.1.100,4.0.0)"
    ]; } ]; }
    { groupId = "org.eclipse.text"; artifactId = "org.eclipse.text"; versions = [ { v="3.5.101"; sha256="1yz5z0nkv5hn3a71ypa1dj0083bvg8iq04cswr5nfxm8m02rrs1g"; deps = [
      self."org.eclipse.core:org.eclipse.core.commands"."3.6.0"
      self."org.eclipse.equinox:org.eclipse.equinox.common"."3.6.0"
    ]; } ]; }
    { groupId = "org.eclipse.jgit"; artifactId = "org.eclipse.jgit"; versions = [ { v="3.2.0.201312181205-r"; sha256="1f4kykjrn5a3wy6f021f836pk0z3yq3xryj62xrz0a8cfrkx3kax"; deps = [
      self."org.eclipse.jgit:org.eclipse.jgit-parent"."3.2.0.201312181205-r"
      self."com.jcraft:jsch"."0.1.46"
    ]; } ]; }
    { groupId = "org.eclipse.jgit"; artifactId = "org.eclipse.jgit-parent"; versions = [ { v="3.2.0.201312181205-r"; sha256="17z1acffmrd23y3z4sa0n54afh69nvh7ixvapvhay8gjnd8jj09m"; } ]; }
    { groupId = "org.eclipse.equinox"; artifactId = "common"; versions = [ { v="3.6.200-v20130402-1505"; sha256="0cp9h6fw7ggw721sr049z8py0z2hn8xga8mhdbivqijl6ckhss0f"; } ]; }
    { groupId = "org.eclipse.equinox"; artifactId = "preferences"; versions = [ { v="3.5.100-v20130422-1538"; sha256="0xhjxqpiaa5qs2gsqjjar937sn5dr09n3hdd2874yzvcspw1v59f"; } ]; }
    { groupId = "org.eclipse.equinox"; artifactId = "registry"; versions = [ { v="[3.2.0,4.0.0)"; sha256="103y36cjrdv7bgd7gnbnmpjm5qpv2vdl4zf09hch5k6bqpph7faw"; chosen = "3.5.400-v20140428-1507"; } ]; }
    { groupId = "org.eclipse.equinox"; artifactId = "org.eclipse.equinox.common"; versions = [ { v="3.6.0"; sha256="0k2fq4lz0lw1jf09abrbj8wy44si7x43vv7vf6vllismk3a75gmz"; } ]; }
    { groupId = "org.eclipse.equinox"; artifactId = "app"; versions = [ { v="[1.0.0,2.0.0)"; sha256="1x76yi0ravz16h2mzlhwllglcgxa9aqjyx5izss02awzdcqyh7d0"; chosen = "1.3.200-v20130910-1609"; } ]; }
    { groupId = "org.eclipse.update"; artifactId = "configurator"; versions = [ { v="[3.1.100,4.0.0)"; sha256="0461bb00mj7abhxamwqma737k6yjvjkdll1ify0m8lr8lajl52cq"; chosen = "3.2.100-v20070615"; } ]; }
    { groupId = "org.apache.httpcomponents"; artifactId = "httpcore"; versions = [ { v="4.3.2"; sha256="0pqn1yq8c8x861qb3gdfp10r039gjzbvpm0yvfn4yz93pjxral80"; deps = [
      self."org.apache.httpcomponents:httpcomponents-core"."4.3.2"
    ]; } ]; }
    { groupId = "org.apache.httpcomponents"; artifactId = "httpcomponents-core"; versions = [ { v="4.3.2"; sha256="1va6rvg516c3j26afx30pdf6kz7qwpjlq05nzr7xxy16rpxq5w47"; } ]; }
    { groupId = "commons-logging"; artifactId = "commons-logging"; versions = [ { v="1.1.3"; sha256="0jwqbr62i5fa8sc0r6yg293fvpa060kjy2bh48rsz5g8ra6i25hl"; deps = [
      self."org.apache.commons:commons-parent"."28"
    ]; } ]; }
    { groupId = "commons-codec"; artifactId = "commons-codec"; versions = [
      { v="1.6"; sha256="0qxpbyklda7kba1yzjnab527c008x6lbbah2r43vnigx1czaj508"; deps = [ self."org.apache.commons:commons-parent"."22" ]; }
      { v="1.9"; sha256="1dkhhk2j03a9hf95gh0wf1dv4q8qphxm4jhyk6r6ka72d3fvxks7"; deps = [ self."org.apache.commons:commons-parent"."32" ]; }
    ]; }
    { groupId = "com.jcraft"; artifactId = "jsch"; versions = [ { v="0.1.46"; sha256="1gpl815dlmz5rc4vc8k6h0d7hxw5kl16vgkxri5r8isifx2m1vkk"; deps = [
      self."org.sonatype.oss:oss-parent"."6"
    ]; } ]; }
    { groupId = "net.minecraftforge"; artifactId = "forge"; versions = [ { v="1.7.10-10.13.4.1448-1.7.10"; sha256="1v6ll8qbv9bfl4j3rs2sd1w23p5p8qhr5ma5y8c98pdkcdjzyln7"; suffix = "-userdev"; } ]; }
    { groupId = "tv.twitch"; artifactId = "twitch"; versions = [ { v="5.16"; sha256="192ywvfv214jdab3prq6jrwsjcy9yiqhvbqsdp98y56sj0qzq055"; deps = [
      self."tv.twitch:twitch-platform"."5.16"
      self."tv.twitch:twitch-external-platform"."4.5"
    ]; } ]; }
    { groupId = "tv.twitch"; artifactId = "twitch-platform"; versions = [ { v="5.16"; sha256="0sx3iqhsyyhdd9y8p4fcl5ww3kjm88x54mqw0m2f045d72xqkw6m"; } ]; }
    { groupId = "tv.twitch"; artifactId = "twitch-external-platform"; versions = [ { v="4.5"; sha256="0rjaqhg760y6kywqkh1zxggyfmbxlsi4mb7b909q2ilpixcc6lka"; } ]; }
    { groupId = "codechicken"; artifactId = "ForgeMultipart"; versions = [ { v="1.7.10-1.2.0.345"; sha256="045fqb0w74h2s93mnfzjzcpca9d462kpa90rng57fhy3zv0p5dnj"; suffix = "-dev"; } ]; }
    { groupId = "codechicken"; artifactId = "CodeChickenLib"; versions = [ { v="1.7.10-1.1.3.138"; sha256="0vx6fjpcqk00i6sb8va68gm68m9l8mwxzkslfg467wmsf310mmag"; suffix="-dev"; } ]; }
    { groupId = "codechicken"; artifactId = "CodeChickenCore"; versions = [ { v="1.7.10-1.0.7.47"; sha256="1jyfk4k487w1dw72wqi78i0d1jv6ksqy7whp8qm5ksbqih1cf5n1"; suffix="-dev"; } ]; }
    { groupId = "codechicken"; artifactId = "NotEnoughItems"; versions = [ { v="1.7.10-1.0.5.111"; sha256="0d6x3611a2pbmhr1d50zxxv8gfnxcmg7v0fsjzd2wwxhxrhcbl4j"; suffix="-dev"; } ]; }
    { groupId = "com.mod-buildcraft"; artifactId = "buildcraft"; versions = [ { v="7.0.9"; sha256="110vfylh7dgr6ffqq9zdb0shjc8vqnfr61rrdicly6n5nw3qb87r"; suffix="-dev"; } ]; }
    { groupId = "pneumaticCraft"; artifactId = "PneumaticCraft-1.7.10"; versions = [ { v="1.9.15-105"; sha256="191k4m3j7fkf7grvgfbl46lnrn3s7hm35914h1ymmck1h89dvkja"; suffix = "-api"; } ]; }
    { groupId = "li.cil.oc"; artifactId = "OpenComputers"; versions = [ { v="MC1.7.10-1.5.12.26"; sha256="1f6xjp3bmwv28qvnr25wiy3jap5ky1zvllyxki4sxx4wm7gxdzi1"; suffix = "-api"; } ]; }
    { groupId = "net.industrial-craft"; artifactId = "industrialcraft-2"; versions = [ { v="2.2.717-experimental"; sha256="1fhjr8vmfk0k67zbs2f4rr5mqc2xy087kxvxq7gyp1cdgwpch2hi"; suffix = "-api"; } ]; }
  ];
in
import ./root.nix { input = /tmp/output.json; inherit deps; }

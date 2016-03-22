#!/bin/sh 
set -e
eval `sed -n -e 's,^\([^=]*\)[.],\1_,g' -e 's,^\([^=]*\)[.],\1_,g' -e 's,^\([^=]*\)[.],\1_,g' -e 's,^\([^=]*\)[.],\1_,g' -e 's,^\([^=]*\)[.],\1_,g' -e 's,^\([^#=]*\)[ ]*=[ "]*\(.*[^ "]\)[" ]*,\1="\2";,p' < build.properties`
OPTIONS=`sed -n -e 's,addon[.].*=\(.*\),\1,p' < build.properties`
PACKAGES=`sed -n -e 's,package[.].*[.]dir=\(.*\),\1/*.java,p' < build.properties`
if [ -d ${build_dir} ]
then
  rm -rf ${build_dir}
fi
mkdir ${build_dir}
mkdir ${release_classes_dir}
mkdir ${build_dir}/META-INF
images=''
for i in $PACKAGES $OPTIONS
do
  files=`echo $files src/$i`
done
( set -x
javac -Xlint:depreciation -O -source ${javac_source} -target ${javac_target} -g:none -deprecation -d ${release_classes_dir} $files )
images=`cd src 2>>/dev/null 1>>/dev/null;echo com/lizardtech/*/*/*.djvu`
for i in $images ; do
  if [ -r "src/$i" ]
  then
    j=${release_classes_dir}/`dirname "$i"`
    if [ -d "$j" ]
    then
      (set -x; cp -v "src/$i" "$j/.")
    fi
  fi
done
cd ${build_dir}
cat <<+ > META-INF/MANIFEST.MF
Main-Class: $package_djview_frame_main
Class-Path: $jarfile_name

Name: $package_djview_dir/
Specification-Title: $package_djview_title
Specification-Version: $version
Specification-Vender: $package_djview_vender
Implementation-Title: $package_djview_name
Implementation-Version: $build
Implementation-Vender: $package_djview_author
+
( set -x
jar cfm ${jarframe_name} META-INF/MANIFEST.MF -C classes ${package_djview_frame_dir} )
rm -rf classes/${package_djview_frame_dir}
cat <<+ > META-INF/MANIFEST.MF
Main-Class: $package_djview_main

Name: com.lizardtech.djvubean.DjVuBean.class
JavaBean: True

Name: $package_djvu_dir/
Specification-Title: $package_djvu_title
Specification-Version: $version
Specification-Vender: $package_djvu_title
Implementation-Title: $package_djvu_name
Implementation-Version: $build
Implementation-Vender: $package_djvu_author

Name: $package_djvubean_dir/
Specification-Title: $package_djvubean_title
Specification-Version: $version
Specification-Vender: $package_djvubean_vender
Implementation-Title: $package_djvubean_name
Implementation-Version: $build
Implementation-Vender: $package_djvubean_author

Name: $package_djview_dir/
Specification-Title: $package_djview_title
Specification-Version: $version
Specification-Vender: $package_djview_vender
Implementation-Title: $package_djview_name
Implementation-Version: $build
Implementation-Vender: $package_djview_author
+
for i in classes/*.class
do
  if [ -r "$i" ]
  then
    mv "$i" .
  fi
done
( set -x
jar cfm ${jarfile_name} META-INF/MANIFEST.MF -C classes com )
rm -rf classes META-INF

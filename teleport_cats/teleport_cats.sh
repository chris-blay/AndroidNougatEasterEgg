#!/bin/bash

# Copyright (C) 2017 Christopher Blay <chris.b.blay@gmail.com>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Exit immediately if anything goes wrong.
set -eu

# Prevent running this script somewhere it might mess stuff up.
for FILE in *; do
    if [ "$FILE" != 'teleport_cats.sh' \
            -a "$FILE" != 'abe.jar' \
            -a "$FILE" != 'adb' ]; then
        echo 'Please run this script in a mostly empty directory. The'
        echo 'directory must contain Android Backup Extractor JAR named'
        echo '"abe.jar". It may optionally contain this script only if it is'
        echo 'named "teleport_cats.sh". It may optionally contain an "adb"'
        echo 'executable which will be used instead of the one on $PATH.'
        echo 'Delete or move all other files and directories as they may be'
        echo 'overwritten, modified, or deleted.'
        exit
    fi
done

# Make sure "abe.jar" is setup properly.
if [ ! -f 'abe.jar' ]; then
    echo 'Can not find regular file "abe.jar" in this directory.'
    echo 'Please download from https://sourceforge.net/projects/adbextractor/'
    exit
fi
if [ ! -x 'abe.jar' ]; then
    echo '"abe.jar" is not executable. Please `chmod +x abe.jar` if you'
    echo 'trust it and want it to be executed by this script.'
    exit
fi

# Make sure Java is setup properly.
if [ "`which java`" == '' ]; then
    echo 'No Java executable on $PATH. Please install Java runtime.'
    exit
fi

# Make sure ADB is setup properly.
if [ -f 'adb' ]; then
    if [ -x 'adb' ]; then
        echo 'Using "adb" from this directory.'
        ADB='./adb'
    else
        echo '"adb" exists in this directory but is not executable.'
        echo 'Please `chmod +x adb` if you trust it and want it to be'
        echo 'executed by this script. Otherwise delete or move it.'
        exit
    fi
else
    if [ "`which adb`" == '' ]; then
        echo 'No ADB executable on $PATH or in this directory.'
        echo 'Please either install Android SDK platform tools and put them'
        echo 'on your $PATH or place an executable "adb" in this directory.'
        exit
    else
        echo 'Using "adb" from $PATH.'
        ADB='adb'
    fi
fi

# Retrieve, unpack, and untar system easter egg backup.
$ADB backup -f com.android.egg.ab com.android.egg
java -jar abe.jar unpack com.android.egg.ab com.android.egg.tar
tar -xf com.android.egg.tar
rm com.android.egg.tar

# Turn unpacked system backup into one for this app.
cd apps/com.android.egg
echo "1
com.covertbagel.neko
1
25
com.android.vending
0
1
308201ad30820116a00302010202044f1125d0300d06092a864886f70d0101050500301b31193017060355040313104368726973746f7068657220426c6179301e170d3132303131343036353035365a170d3337303130373036353035365a301b31193017060355040313104368726973746f7068657220426c617930819f300d06092a864886f70d010101050003818d00308189028181009900480d88c8a22794c2de266e57e3286bb61f4804ee265cd0e11ca6936186a2ccd91da89fa65a28f428dd9ccaf0fc23e96ff6559e021e0dd6ac17757c176968aea99fef6180b8a14d59e4e1ee4b4840dacd49dda641e56cbf75d99cb37cab965a27a54419c316d78915bf9dbad5e8df7cbb70c57b946144fa607fe4a45d22b10203010001300d06092a864886f70d010105050003818100228f0ec9660a6fe6029305df034f8e43e31cf0f80ec132384049442353ec940f4a38e04dcce1403b13719fdb062ec1a1c158bbb6dcffb98a42318da1cc99ff6291398bb314accf8c43e6f96332e62c808eb0558a75fc5bcffddf312043158e7f40b2c7c2783d0bc1cc169f72e53fca9db220bb9714bad0f30c83d658a479f698" > _manifest
cd ..
mv com.android.egg com.covertbagel.neko
cd ..

# Tar and pack modified backup into one for this app.
tar -cf com.covertbagel.neko.tar \
    apps/com.covertbagel.neko/_manifest apps/com.covertbagel.neko/sp/mPrefs.xml
rm -rf apps
java -jar abe.jar pack-kk com.covertbagel.neko.tar com.covertbagel.neko.ab
rm com.covertbagel.neko.tar

# Restore backup into this app.
$ADB restore com.covertbagel.neko.ab


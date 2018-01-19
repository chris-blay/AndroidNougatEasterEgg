#!/bin/bash

# Copyright (C) 2017, 2018 Christopher Blay <chris.b.blay@gmail.com>
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

set -eu

ANDROID_SDK=/opt/android-sdk
BUILD_TOOLS_VERSION=27.0.3

if [ $# -ne 1 ]; then
    echo "Usage: $0 keystore"
    exit
else
    if [ -f "$1" ]; then
        KEYSTORE="$1"
    else
        echo "Specified keystore does not exist"
        exit
    fi
fi

cd workspace
bazel build -c opt :AndroidNougatEasterEgg
cp --no-preserve=mode bazel-bin/AndroidNougatEasterEgg_unsigned.apk ..
cp --no-preserve=mode bazel-bin/AndroidNougatEasterEgg_proguard.map ..
cd ..
"$ANDROID_SDK/build-tools/$BUILD_TOOLS_VERSION/zipalign" -v -p 4 \
    AndroidNougatEasterEgg_unsigned.apk \
    AndroidNougatEasterEgg_unsigned_aligned.apk
rm AndroidNougatEasterEgg_unsigned.apk
"$ANDROID_SDK/build-tools/$BUILD_TOOLS_VERSION/apksigner" sign \
    --ks "$KEYSTORE" --out AndroidNougatEasterEgg_signed.apk \
    AndroidNougatEasterEgg_unsigned_aligned.apk
rm AndroidNougatEasterEgg_unsigned_aligned.apk
mv AndroidNougatEasterEgg_signed.apk AndroidNougatEasterEgg.apk

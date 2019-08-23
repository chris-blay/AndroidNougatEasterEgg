# Copyright (C) 2017, 2019 Christopher Blay <chris.b.blay@gmail.com>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

deps = [
    "@androidsdk//com.android.support:recyclerview-v7-25.3.1",
    "@androidsdk//com.android.support:support-annotations-25.3.1",
    "@androidsdk//com.android.support:support-compat-25.3.1",
    "@androidsdk//com.android.support:support-core-ui-25.3.1",
]

android_binary(
    name = "AndroidNougatEasterEggDev",
    srcs = glob(["src/**/*.java"]),
    custom_package = "com.covertbagel.neko",
    manifest = "AndroidManifestV29.xml",
    resource_files = glob(["res/**"]),
    deps = deps,
)

android_binary(
    name = "AndroidNougatEasterEggV24",
    srcs = glob(["src/**/*.java"]),
    custom_package = "com.covertbagel.neko",
    manifest = "AndroidManifestV24.xml",
    proguard_generate_mapping = 1,
    proguard_specs = ["release.proguard"],
    resource_files = glob(["res/**"]),
    deps = deps,
)

android_binary(
    name = "AndroidNougatEasterEggV29",
    srcs = glob(["src/**/*.java"]),
    custom_package = "com.covertbagel.neko",
    manifest = "AndroidManifestV29.xml",
    proguard_generate_mapping = 1,
    proguard_specs = ["release.proguard"],
    resource_files = glob(["res/**"]),
    deps = deps,
)

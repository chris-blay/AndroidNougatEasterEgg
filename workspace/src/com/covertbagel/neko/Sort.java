/*
 * Copyright (C) 2017 Christopher Blay <chris.b.blay@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.covertbagel.neko;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static com.covertbagel.neko.Sort.BODY_HUE;
import static com.covertbagel.neko.Sort.CHRONOLOGICAL;
import static com.covertbagel.neko.Sort.LEVEL;
import static com.covertbagel.neko.Sort.NAME;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Retention(SOURCE)
@IntDef({CHRONOLOGICAL, BODY_HUE, NAME, LEVEL})
@interface Sort {
    int CHRONOLOGICAL = 0;
    int BODY_HUE = 1;
    int NAME = 2;
    int LEVEL = 3;
}

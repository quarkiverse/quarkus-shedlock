/**
 * Copyright 2009 the original author or authors.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
// https://github.com/lukas-krecan/ShedLock/blob/master/cdi/shedlock-cdi/src/main/java/net/javacrumbs/shedlock/cdi/internal/LockingNotSupportedException.java
package io.quarkiverse.shedlock.common.runtime;

import net.javacrumbs.shedlock.support.LockException;

public class LockingNotSupportedException extends LockException {
    LockingNotSupportedException() {
        super("Can not lock method returning value (do not know what to return if it's locked)");
    }
}

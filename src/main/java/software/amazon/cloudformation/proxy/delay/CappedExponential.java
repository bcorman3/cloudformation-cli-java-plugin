/*
* Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License").
* You may not use this file except in compliance with the License.
* A copy of the License is located at
*
*  http://aws.amazon.com/apache2.0
*
* or in the "license" file accompanying this file. This file is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied. See the License for the specific language governing
* permissions and limitations under the License.
*/
package software.amazon.cloudformation.proxy.delay;

import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class CappedExponential extends MinDelayAbstractBase {

    final double powerBy;

    final long startTimeInMillis;

    final Duration maxDelay;

    CappedExponential(Duration timeout,
                      Duration minDelay,
                      Double powerBy,
                      Duration maxDelay) {
        super(timeout, minDelay);
        Preconditions.checkArgument(powerBy >= 1.0, "powerBy >= 1.0");
        Preconditions.checkArgument(maxDelay != null && maxDelay.toMillis() >= 0, "maxDelay must be > 0");
        Preconditions.checkArgument(maxDelay.compareTo(minDelay) >= 0, "maxDelay.compareTo(minDelay) >= 0");
        this.powerBy = powerBy;
        this.maxDelay = maxDelay;
        this.startTimeInMillis = System.currentTimeMillis();
    }

    public static Builder of() {
        return new Builder();
    }

    public static final class Builder extends MinDelayBasedBuilder<CappedExponential, Builder> {
        private double powerBy = 2;
        private Duration maxDelay = Duration.ofSeconds(20);

        public CappedExponential.Builder powerBy(Double powerBy) {
            this.powerBy = powerBy;
            return this;
        }

        public CappedExponential.Builder maxDelay(Duration maxDelay) {
            this.maxDelay = maxDelay;
            return this;
        }

        @Override
        public CappedExponential build() {
            return new CappedExponential(timeout, minDelay, powerBy, maxDelay);
        }
    }

    @Override
    public Duration nextDelay(int attempt) {
        if (attempt == 0) {
            return minDelay;
        }
        if (System.currentTimeMillis() - startTimeInMillis > timeout.toMillis()) {
            return Duration.ZERO;
        }
        long next = Math.round(minDelay.toMillis() * Math.pow(powerBy, attempt - 1));
        return Duration.ofSeconds(Math.min(maxDelay.getSeconds(), TimeUnit.MILLISECONDS.toSeconds(next)));
    }

}

package org.rubix.redfile;

public interface IMixinEntity {
    void redfile$setSampleCount(long trialNumber, long amount);
    void redfile$incSampleCount(long trialNumber);
    long redfile$getSamples(long trialNumber);
}

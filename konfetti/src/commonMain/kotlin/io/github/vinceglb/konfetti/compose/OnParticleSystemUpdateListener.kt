package io.github.vinceglb.konfetti.compose

import io.github.vinceglb.konfetti.core.PartySystem

public interface OnParticleSystemUpdateListener {
    public fun onParticleSystemEnded(
        system: PartySystem,
        activeSystems: Int,
    )
}

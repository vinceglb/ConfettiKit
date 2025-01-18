package io.github.vinceglb.confettikit.compose

import io.github.vinceglb.confettikit.core.PartySystem

public interface OnParticleSystemUpdateListener {
    public fun onParticleSystemEnded(
        system: PartySystem,
        activeSystems: Int,
    )
}

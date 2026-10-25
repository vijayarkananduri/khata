package com.khata.domain.stats

/** Returns a finite 0..1 ratio; no budget or negative inputs produce an empty ring. */
fun budgetProgressFraction(spentPaise: Long, budgetPaise: Long): Float {
    if (budgetPaise <= 0L || spentPaise <= 0L) return 0f
    return (spentPaise.toDouble() / budgetPaise.toDouble()).coerceIn(0.0, 1.0).toFloat()
}

package com.moulberry.lattice.element;

import com.moulberry.lattice.LatticeDynamicFrequency;

import java.util.function.BooleanSupplier;

public record LatticeDynamicCondition(BooleanSupplier condition, LatticeDynamicFrequency frequency) {
}

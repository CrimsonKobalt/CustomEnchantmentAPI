package db.chris.customenchantment.anvil;

import db.chris.customenchantment.anvil.implementations.previouswork.FreePreviousWorkCostPolicy;
import db.chris.customenchantment.anvil.implementations.previouswork.VanillaPreviousWorkCostPolicy;

@FunctionalInterface
public interface PreviousWorkCostPolicy {

    int cost(int previousAnvilUses);

    PreviousWorkCostPolicy VANILLA = new VanillaPreviousWorkCostPolicy();
    PreviousWorkCostPolicy FREE = new FreePreviousWorkCostPolicy();
}

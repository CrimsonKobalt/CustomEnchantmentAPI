package db.chris.customenchantment.anvil.implementations.previouswork;

import db.chris.customenchantment.anvil.PreviousWorkCostPolicy;

public class FreePreviousWorkCostPolicy implements PreviousWorkCostPolicy {

    @Override
    public int cost(int previousAnvilUses) {
        return 0;
    }
}

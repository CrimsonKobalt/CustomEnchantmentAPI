package db.chris.customenchantment.anvil.configuration.previouswork.policy;

import db.chris.customenchantment.anvil.configuration.previouswork.PreviousWorkCostPolicy;

public class FreePreviousWorkCostPolicy implements PreviousWorkCostPolicy {

    @Override
    public int cost(int previousAnvilUses) {
        return 0;
    }
}

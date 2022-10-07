package abyss.plugin.api.plugin.nodes.leafs;

import abyss.plugin.api.Player;
import abyss.plugin.api.Players;
import abyss.plugin.api.actions.PlayerAction;
import abyss.plugin.api.plugin.nodes.SpiritNode;

public class PlayerNode extends SpiritNode<PlayerAction, Player> {

    public PlayerNode(Player entity, PlayerAction action) {
        super(entity, action);
    }

    @Override
    public boolean exists() {
        return Players.byServerIndex(getEntity().getServerIndex()) != null;
    }
}

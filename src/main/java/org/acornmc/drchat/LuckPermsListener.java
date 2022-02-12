package org.acornmc.drchat;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import static org.bukkit.Bukkit.getServer;

public class LuckPermsListener {

    private final DrChat plugin;
    private final LuckPerms luckPerms;

    public LuckPermsListener(DrChat plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    public void register() {
        EventBus eventBus = this.luckPerms.getEventBus();
        eventBus.subscribe(this.plugin, NodeRemoveEvent.class, this::onNodeRemove);
    }

    private void onNodeRemove(NodeRemoveEvent event) {
        // TODO remove after confirmation of working
        Util.log("NodeRemoveEvent was fired");
        User target = (User) event.getTarget();
        OfflinePlayer player = getServer().getOfflinePlayer(target.getUniqueId());
        Node node = event.getNode();
        if (node instanceof InheritanceNode) {
            String groupName = ((InheritanceNode) node).getGroupName();
            // TODO make this a list of strings instead of these
            if (groupName.equalsIgnoreCase("vip") || groupName.equalsIgnoreCase("dragon")) {
                // the player was removed from vip
                // TODO remove after confirmation of working
                Util.log("Removing prefix from player");
                // TODO make this a list of commands
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " meta removeprefix 1");
            }
        }
    }
}

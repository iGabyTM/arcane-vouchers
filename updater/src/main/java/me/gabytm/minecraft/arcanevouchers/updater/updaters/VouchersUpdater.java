package me.gabytm.minecraft.arcanevouchers.updater.updaters;

import me.gabytm.minecraft.arcanevouchers.updater.utils.Files;
import me.gabytm.minecraft.arcanevouchers.updater.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class VouchersUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(VouchersUpdater.class);

    private final Pattern actionPattern = Pattern.compile("\\[(?<id>\\w+)] (?<data>.*)");
    private final Pattern randomNumberTag = Pattern.compile("\\{random:(\\d+,\\d+)}");
    private final Pattern randomElementPattern = Pattern.compile("\\{random:([^}]+)}");

    public VouchersUpdater() {
        final File vouchersFile = new File("vouchers.yml");

        if (!Files.checkFile(vouchersFile)) {
            return;
        }

        try {
            Files.copy("vouchers.yml");
            final YamlConfigurationLoader configurationLoader = YamlConfigurationLoader.builder()
                    .file(vouchersFile)
                    .headerMode(HeaderMode.PRESERVE)
                    .nodeStyle(NodeStyle.BLOCK)
                    .indent(2)
                    .build();
            final CommentedConfigurationNode config = configurationLoader.load();

            if (update(config)) {
                configurationLoader.save(config);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private boolean update(final CommentedConfigurationNode config) throws SerializationException {
        final CommentedConfigurationNode vouchersSection = config.node("vouchers");

        if (vouchersSection.empty() || vouchersSection.virtual() || !vouchersSection.isMap()) {
            LOGGER.warn("Could not find 'vouchers' section");
            return false;
        }

        for (final CommentedConfigurationNode voucherSection : vouchersSection.childrenMap().values()) {
            final CommentedConfigurationNode itemNode = voucherSection.node("item");

            if (!itemNode.empty() && !itemNode.virtual()) {
                updateItem(itemNode);
            }

            final CommentedConfigurationNode settingsNode = voucherSection.node("settings");

            if (!settingsNode.empty() && !settingsNode.virtual()) {
                updateSettings(settingsNode);
            }

            final CommentedConfigurationNode actionsNode = voucherSection.node("actions");

            if (actionsNode.isList()) {
                final List<String> actions = updateActions(actionsNode.getList(String.class));
                actionsNode.set(actions);
                voucherSection.node("bulkActions").set(actions);
            }
        }

        return true;
    }

    private void updateItem(final CommentedConfigurationNode node) throws SerializationException {
        node.node("material").act(materialNode -> {
            if (materialNode.empty()) {
                return;
            }

            final String material = materialNode.getString();

            if (material.startsWith("head;")) {
                LOGGER.info("Replacing {} with PLAYER_HEAD (if you are on pre 1.13 you need SKULL_ITEM and 'damage:3') and setting the texture on its own key", material);
                materialNode.set(material);
                node.node("texture").set(material.split(";")[1]);
            }
        });

        final String itemName = node.node("display_name").getString();

        if (itemName != null) {
            node.removeChild("display_name");
            node.node("name").set(Strings.upgradeColorsFormat(itemName));
        }

        node.node("lore").act(loreNode -> {
            if (!loreNode.isList() || loreNode.empty()) {
                return;
            }

           loreNode.set(Strings.upgradeColorsFormat(loreNode.getList(String.class)));
        });
    }

    private void updateSettings(final CommentedConfigurationNode node) throws SerializationException {
        // bulk open
        node.node("bulkOpen", "enabled").set(node.node("bulkOpen").getBoolean());
        node.removeChild("bulkOpen");

        // whitelisted worlds
        final CommentedConfigurationNode worldWhitelistNode = node.node("worldWhitelist");

        if (!worldWhitelistNode.virtual()) {
            // Worlds list
            final CommentedConfigurationNode listNode = worldWhitelistNode.node("list");

            if (listNode.isList()) {
                final List<String> list = listNode.getList(String.class);

                if (list != null && !list.isEmpty()) {
                    node.node("worlds", "whitelist", "list").set(list);
                }
            }

            // Message
            final String message = worldWhitelistNode.node("message").getString();

            if (message != null) {
                worldWhitelistNode.removeChild("message");
                node.node("worlds", "whitelist", "message").set(Strings.upgradeColorsFormat(message));
            }

            node.removeChild("worldWhitelist");
        }
        //-----
    }

    private List<String> updateActions(final List<String> actions) {
        final List<String> updatedActions = new ArrayList<>(actions.size());

        for (final String action : Strings.upgradeColorsFormat(actions)) {
            final Matcher matcher = this.actionPattern.matcher(action);

            if (!matcher.matches()) {
                LOGGER.warn("Action '{}' doesn't match the format", action);
                continue;
            }

            final String id = matcher.group("id");
            final String data = replaceTags(matcher.group("data"));

            switch (id.toLowerCase()) {
                case "actionbar": {
                    updatedActions.add("{type=ACTION} [message] " + data);
                    break;
                }

                case "broadcast": {
                    updatedActions.add("{broadcast=*} [message] " + data);
                    break;
                }

                case "chat": {
                    updatedActions.add("{type=PLAYER} [message] " + data);
                    break;
                }

                case "permission": {
                    final String[] parts = data.split(" ", 2);

                    if (parts.length != 2) {
                        continue;
                    }

                    updatedActions.add(format("{permission=~%s} [player] %s", parts[0], parts[1]));
                    break;
                }

                case "permissionbroadcast": {
                    final String[] parts = data.split(" ", 2);

                    if (parts.length != 2) {
                        continue;
                    }

                    updatedActions.add(format("{broadcast=permission:%s} [message] %s", parts[0], parts[1]));
                    break;
                }

                case "sound": {
                    final String[] parts = data.split(" ", 3);

                    switch (parts.length) {
                        case 1: {
                            updatedActions.add("[sound] " + parts[0]);
                            break;
                        }

                        case 2: {
                            updatedActions.add(format("{volume=%s} [sound] %s", parts[1], parts[0]));
                        }

                        case 3: {
                            updatedActions.add(format("{volume=%s pitch=%s} [sound] %s", parts[1], parts[2], parts[0]));
                            break;
                        }
                    }

                    break;
                }

                // addmoney, console, message, player
                default: {
                    updatedActions.add(format("[%s] %s", id, data));
                    break;
                }
            }
        }

        return updatedActions;
    }

    private String replaceTags(String string) {
        string = randomNumberTag.matcher(string).replaceAll("randomL:{$1}");
        string = randomElementPattern.matcher(string).replaceAll("randomE:{$1}");
        return string;
    }

}

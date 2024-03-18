package me.refracdevelopment.simpletags.commands;

import me.refracdevelopment.simpletags.SimpleTags;
import me.refracdevelopment.simpletags.manager.configuration.ConfigFile;
import me.refracdevelopment.simpletags.utilities.Permissions;
import me.refracdevelopment.simpletags.utilities.chat.Color;
import me.refracdevelopment.simpletags.utilities.chat.Placeholders;
import me.refracdevelopment.simpletags.utilities.chat.StringPlaceholders;
import me.refracdevelopment.simpletags.utilities.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DeleteCommand extends SubCommand {

    /**
     * @return The name of the subcommand
     */
    @Override
    public String getName() {
        return SimpleTags.getInstance().getCommands().DELETE_COMMAND_NAME;
    }

    /**
     * @return The aliases that can be used for this command. Can be null
     */
    @Override
    public List<String> getAliases() {
        return SimpleTags.getInstance().getCommands().DELETE_COMMAND_ALIASES;
    }

    /**
     * @return A description of what the subcommand does to be displayed
     */
    @Override
    public String getDescription() {
        return SimpleTags.getInstance().getLocaleFile().getString("command-delete-description");
    }

    /**
     * @return An example of how to use the subcommand
     */
    @Override
    public String getSyntax() {
        return "<identifier>";
    }

    /**
     * @param commandSender The thing that ran the command
     * @param args          The args passed into the command when run
     */
    @Override
    public void perform(CommandSender commandSender, String[] args) {
        if (!commandSender.hasPermission(Permissions.DELETE_COMMAND)) {
            Color.sendMessage(commandSender, "no-permission");
            return;
        }

        if (args.length != 2) {
            Color.sendMessage(commandSender, "usage", StringPlaceholders.builder()
                    .add("cmd", getName()).add("args", getSyntax()).build());
            return;
        }

        // Delete a tag
        String configName = args[1];

        if (SimpleTags.getInstance().getTagManager().getCachedTag(configName) == null) {
            Color.sendMessage(commandSender, "invalid-tag", Placeholders.setPlaceholders(commandSender));
            return;
        }

        StringPlaceholders placeholders = StringPlaceholders.builder()
                .addAll(Placeholders.setPlaceholders(commandSender))
                .add("tag-name", SimpleTags.getInstance().getTagManager().getCachedTag(configName).getTagName())
                .add("tag-prefix", SimpleTags.getInstance().getTagManager().getCachedTag(configName).getTagPrefix())
                .build();

        ConfigFile tagsFile = SimpleTags.getInstance().getTagsFile();
        tagsFile.remove("tags." + configName);
        tagsFile.save();
        tagsFile.reload();
        SimpleTags.getInstance().getTags().loadConfig();

        SimpleTags.getInstance().getTagManager().removeTag(SimpleTags.getInstance().getTagManager().getCachedTag(configName));
        SimpleTags.getInstance().getTagManager().updateTags();

        Color.sendMessage(commandSender, "tag-deleted", placeholders);
    }

    /**
     * @param player The player who ran the command
     * @param args   The args passed into the command when run
     * @return A list of arguments to be suggested for autocomplete
     */
    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        List <String> tagNames = SimpleTags.getInstance().getTagManager().getTagNames();

        if (args.length == 2) {
            return tagNames;
        }
        return null;
    }
}
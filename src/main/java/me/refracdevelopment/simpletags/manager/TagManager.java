package me.refracdevelopment.simpletags.manager;

import lombok.Getter;
import me.refracdevelopment.simpletags.SimpleTags;
import me.refracdevelopment.simpletags.player.data.Tag;
import me.refracdevelopment.simpletags.utilities.chat.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class TagManager {

    private final List<Tag> loadedTags = new ArrayList<>();

    public void loadTags() {
        getLoadedTags().clear();
        SimpleTags.getInstance().getTagsFile().getSection("tags").getRoutesAsStrings(false).forEach(tag -> {
            addTag(new Tag(tag, SimpleTags.getInstance().getTagsFile().getString("tags." + tag + ".name"),
                    SimpleTags.getInstance().getTagsFile().getString("tags." + tag + ".prefix"),
                    SimpleTags.getInstance().getTagsFile().getString("tags." + tag + ".item")));
        });
        Color.log("&eLoaded " + getLoadedTags().size() + " tags.");
    }

    public void updateTags() {
        SimpleTags.getInstance().getProfileManager().getProfiles().values().forEach(profile -> {
            if (getCachedTag(profile.getData().getTag()) == null) {
                profile.getData().setTag("");
                profile.getData().setTagPrefix("");
                return;
            }

            profile.getData().setTagPrefix(getCachedTag(profile.getData().getTag()).getTagPrefix());
        });
    }

    public void addTag(Tag tag) {
        if (!getLoadedTags().contains(tag)) {
            getLoadedTags().add(tag);
        }
    }

    public void removeTag(Tag tag) {
        getLoadedTags().remove(tag);
    }

    public Tag getCachedTag(String tagName) {
        return getLoadedTags().stream().filter(tag -> tag.getTagName().equalsIgnoreCase(tagName)).findFirst().get();
    }

    public List<String> getTagNames() {
        return getLoadedTags().stream().map(Tag::getConfigName).collect(Collectors.toList());
    }
}
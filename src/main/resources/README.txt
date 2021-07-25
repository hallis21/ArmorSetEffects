# README
# Ignore the config.yml
# These are instructions for how to add your own sets
# Do not edit files in this directory, they are overwritten on every run

# Mutliple examples are included, use the format used in those

# One set per file, do not use the same file
# Use the JSON format shown in the examples
# Cooldown and duration of itemeffects are in seconds
# PotionEffect amplifiers start at 0
# If the effectType is invalid it will be ignored
# If you want a set to contain empty slots (no piece can be equipped in its place)  use AIR, like this 'item: "AIR"' 
# If you want a set to have interchangable pieces (any piece can be equipped in its place) just leave the space empty like this "item: "
# If you want sets to be hidden from /armor-list, set hidded to true (permissions to see all are defualt)
# Lore can be added and searched for, but you need seperate plugins / scripts to add lore to an item
# displayName is the name of the item, it can be set using an Anvil (meaning it can be done in survival)
# To make a set without permanent effects simply leave it empty (permanentEffects: )
# Same goes for item effects ^^
# Multiple effects can be added to a singe item effect
# If you have sets that are the same, but where one has metadata, use the priority system to make sure they work in your favor. Higher priority is better.
# See example 1 and 2 ^^
# If you have sets that have interchangable slots, make sure to prioritise correctly with similar sets
# Note: Priority does nothing if the two sets can't be worn at the same time
# You can add permissions to each set (permission: exampleSetOne), when adding permissions to players use "armorseteffects.sets.{yourPermission}"

# Possible, but not reccomended:
#  - Itemeffects with no item ("item: ") as it will try to enable everytime you rightclick
#  - Using blocks as items in the itemeffects
#  - Using the same name for multiple sets

# To find item names and potion effect types use:
# Do NOT use legacy items (LEGACY_DIAMOND_BOOTS)
# - https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html
# - https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html


# KNOW BUGS / ISSUES:
# If two sets contain the same armor slots, but one of them requires metadata, it is not certain that the one with metadata is applied
# If a set has interchangable slots, and where the other slots are the same as another set, it is not certain which will be applied

# Will work on a fix for these, but with caution they can be avoided
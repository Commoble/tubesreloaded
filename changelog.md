## 4.0.0.0
* Updated to neoforge / MC-1.20.4
* No longer requires or bundles the useitemonblockevent mod as neoforge provides the required functionality

## 3.1.0.0
* Added Multifilters, which can filter up to 27 different items and receive input from tubes/hoppers/etc on all five non-output sides. Unlike normal filters, their internal contents cannot be changed except via the GUI, and they will not accept items while their filter is empty.

## 3.0.0.0
* Updated to minecraft 1.20.1 (requires forge 47.0.3 or higher)
* The useitemonblockevent mod is now bundled with tubes reloaded via forge jar-in-jar (instead of being required to download it separately)

## 2.0.0.2
* Correctly fail modloading when useitemonblockevent isn't present and notify user that it's missing

## 2.0.0.1
* Updated to forge 43.1.0 for 1.19.2
* Removed debug logspam on block place that was accidentally left in

## 2.0.0.0
* Updated to 1.19. This is a breaking change and blockentities in old worlds generally will not preserve their data.

New Features:
* Tubes can now be used in structure nbt, even if they have items inside them or tubing pliers connections to other tubes. Have fun!
* Added tubesreloaded:rotatable_by_pliers block tag, using the tubing pliers on blocks in this tag will cycle through their facing states. This tag includes extractors, filters, loaders, osmosis filters, and shunts. Other blocks can be added to this tag, though only blocks that have the vanilla four-way facing and six-way facing properties are supported.

Data Changes:
* Changed the save format of tube blockentities. They now store positional and rotational data relative to the tube, rather than as absolute world coordinates.
* All tube blocks now have a new blockstate property ("group"), which is only set when the tube is rotated or mirrored by structure pieces or similar things and is used to save and load the tube's remote connections correctly. This property has eight values: `identity`, `rot_180_face_xz`, `rot_90_y_neg`, `rot_90_y_pos`, `invert_x`, `invert_z`, `swap_xz`, and `swap_neg_xz`. Tubes placed by players will only have the "identity" state.
* The tubesreloaded:shunts block tag has been removed

Other Changes:
* Extractors and Loaders now now use the collision shape of the block in front of them instead of the render/occlusion shape to decide whether to eject items or not
* Rotated the filter model to be consistent with hoppers (the wide end is now the input end)
* Updated textures of green, blue, purple, brown, and cyan tubes (made darker and more saturated)
* Shunts, filters, osmosis filters, and distributors can now all insert items into each other.
* Tubes can now be crafted from copper bars (in addition to the gold bars recipe). This yields fewer tubes than crafting them from gold bars as copper isn't as malleable as gold.
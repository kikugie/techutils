# Technical Utilities

Tech Utils is a mod providing client-side tweaks for [Litematica](https://www.curseforge.com/minecraft/mc-mods/litematica), [WorldEdit](https://www.curseforge.com/minecraft/mc-mods/worldedit) and other small utilities.

## Mod config
To access mod config open [Mod Menu](https://modrinth.com/mod/modmenu) and select this mod.

## Features

- Sync WorldEdit selection  
  Snap WorldEdit region to active Litematica selection - `autoWeSync`.  
  *Region synchronizes shortly after you modify a selection, showing you a confirmation message above the hotbar.*

  ![](https://github.com/Kikugie/techutils/raw/main/files/wesync.gif)

- Litematic preview  
  Show 3D render of selected litematic in Load Schematics menu - `renderPreview`.  
  *This only works for .litematic files.*

  ![](https://github.com/Kikugie/techutils/raw/main/files/preview.gif)

- Image selection dialog for litematica preview:  
  Set litematic preview image to one you select, which unlike the 3D preview feature will be shown to any player, even without this mod.  
  *In main Litematica menu go to Schematic Manager, select a litematic and ctrl + click on Set Preview button to open the prompt.*

![](https://cdn.modrinth.com/data/hNoAJSm7/images/8f5b5683c39dba7ce47e9ca89b3ff97b7f6d53e3.png)

- Inventory setup preview:  
  Show items that have to be in a container and highlights wrong or mismatched ones - `inventoryScreenOverlay`.  
  *Item colors match your placement block colors. By default its:  
  - Light blue: missing item;  
  - Orange: mismatched amount or nbt data;  
  - Magenta: extra item that shouldn't be present;  
  - Red: wrong item type.*

![](https://cdn.modrinth.com/data/hNoAJSm7/images/02ec28f6c20a28edd638d91214bfb1967630b9d4.png)

- Compact scoreboard (ported from my old mod) - `compactScoreboard`.  
  Using short number format:
  - 1000 -> 1k
  - 1000000 -> 1M
  - 1000000000 -> 1B
- Disable WorldEdit neighbor updates on log-in - `autoDisableUpdates`.
- Hotkeys for litematic placement rotation & mirror - `rotatePlacement` and `mirrorPlacement`.
- `/isorender selection` shortcut for rendering current Litematica selection in [Isometric Renders](https://modrinth.com/mod/isometric-renders).
- A hotkey to give yourself a container full of the item you're holding - `giveFullInv`. Supports boxes, chests, and bundles. Nesting them is possible.
- A hotkey to refresh the material list - `refreshMaterialList`.
- Make easy place act as if all blocks are full blocks. Useful for placing blocks with small hit boxes.
- *And more to come!*

## Dependencies

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [MaliLib](https://www.curseforge.com/minecraft/mc-mods/malilib)
- [Litematica](https://www.curseforge.com/minecraft/mc-mods/litematica)

*Mod logo by Mizeno*
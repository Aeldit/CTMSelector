# Documentation for the commands of the mod

Commands :

1. /cyan (for description and config editing)
2. Teleportation
3. Misc

## I). /cyan commands

> These commands are for configuration and description

```text
cyan
├── reloadTranslations -> Reloads the translations file
│
├── getConfig -> Displays all the current options values
│
├── config [option] -> If no argument is given, the description and current value of the option are displayed in the player's chat 
│               └── [integer | boolean] -> Sets the given option to the given value
│
└── description
    ├── commands -> If no argument is given after this one, displays the description for all commands
    │   └── [command] -> Displays the description of the given command
    │
    └── options -> If no argument is given after this one, displays the description for all options
        └── [option] -> Displays the description of the given options
```

## II). Teleportation

- /bed → teleports the player executing the command to its bed or respawn anchor
- /surface → teleports the player executing the command to the highest block on its position

## III). Misc

- /kgi -> removes items floating on the ground in the default radius (in chunks), or in the specified radius

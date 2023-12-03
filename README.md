# EnchantedAllies

EnchantedAllies is a Bukkit plugin designed to enhance the multiplayer experience by allowing players to manage friendships within the game. This plugin introduces a suite of features to create, manage, and interact with friends, bringing a social aspect to your server.

## Features

- **Friend Requests**: Players can send, accept, or deny friend requests.
- **Friend List**: Players can view a list of their friends, showing who is online and offline.
- **Friend Removal**: Players have the option to remove friends from their list.
- **Real-time Notifications**: Interactive notifications for friend requests and acceptances.
- **Commands**: Easy-to-use commands with clear instructions.

## Commands

- `/friend add <playerName>` - Send a friend request to a player.
- `/friend accept <playerName>` - Accept a friend request.
- `/friend deny <playerName>` - Deny a friend request.
- `/friend remove <playerName>` - Remove a player from your friends list.
- `/friend list` - Display your current friends list.
- `/friend requests` - Show pending friend requests.

## API Usage

EnchantedAllies uses the following APIs:

- **Bukkit/Spigot API**: For the core Minecraft server functionality.
- **Lamp Command Framework**: To handle complex command structures with ease.
- **Kyori Adventure**: For rich text formatting and interactive components.
- **JavaPlugin**: The base class for Bukkit plugins.

## Installation

1. Download the `EnchantedAllies.jar` file.
2. Place it into your Minecraft server's `plugins` directory.
3. Restart the server, or load the plugin dynamically if your server supports it.

## Configuration

EnchantedAllies comes with a default configuration that should work out of the box for most servers. If you wish to customize the plugin's settings, you can edit the `config.yml` file in the `plugins/EnchantedAllies` directory after the first run.

## Dependencies

This plugin requires a Bukkit-compatible server implementation such as Spigot or Paper.

## Building from Source

If you wish to build EnchantedAllies from source:

1. Clone the repository: `git clone https://github.com/yourusername/EnchantedAllies.git`
2. Navigate to the cloned directory: `cd EnchantedAllies`
3. Build with Maven: `mvn clean install`
4. The built JAR can be found in the `target/` directory.

## Contributing

Contributions to EnchantedAllies are always welcome, whether it be improvements to the code, documentation, or new features. If you are interested in contributing:

1. Fork the repository on GitHub.
2. Create a new branch for your changes.
3. Implement your changes.
4. Submit a pull request with a comprehensive description of the changes.

## License

EnchantedAllies is open-sourced software licensed under the [MIT license](LICENSE).

## Support

If you encounter any issues or require assistance, please submit an issue on the GitHub issue tracker for this project.

---

**Note**: The `README.md` is subject to change. Always keep it updated with the latest changes in your project.

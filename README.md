# Base-API for Minecraft Plugins

Base-API is a utility library for Minecraft server plugins that simplifies common tasks such as registering/unregistering commands dynamically and creating inventories with listeners. This library is designed to help developers quickly implement features without having to write repetitive boilerplate code, allowing them to focus on creating unique gameplay experiences.

## Features

### Dynamic Command Registration
- **Register and Unregister Commands:** Base-API provides a simple and flexible way to register and unregister commands at runtime. This feature is particularly useful for plugins that need to modify their command structure on the fly, such as during reloads or when features are enabled or disabled.

### Enhanced Inventory Management
- **BaseInventory Class:** The BaseInventory class is a powerful tool for creating custom inventories with built-in listeners. It simplifies the process of setting up custom GUIs and managing user interactions with inventory items.

## How to Use

### Command Registration
To register commands, simply instantiate a `CommandRegistry` object and use the `register` method with your `BaseCommand` instances. Unregistering commands is as straightforward as calling the `unregister` method.

```java
CommandRegistry commandRegistry = new CommandRegistry(this); // 'this' refers to your plugin
BaseCommand myCommand = new MyCommand(); // Your command that extends BaseCommand
commandRegistry.register(myCommand);

// To unregister
commandRegistry.unregister(myCommand);
```

### Inventory Creation
Creating a custom inventory involves extending the `BaseInventory` class and implementing its abstract methods. You can easily handle clicks and other interactions by registering callbacks within the custom inventory class.

```java
public class MyInventory extends BaseInventory {
    public MyInventory(/* Your params */) {
        // Initialize BaseInventory with proper parameters
        // Define how your inventory should be set up
    }
    
    // Implement other necessary methods
}
```

To open your custom inventory for a player:

```java
MyInventory inventory = new MyInventory(/* Your params */);
inventory.open(player);
```

## Dependencies

Base-API is built on top of the [Spigot API](https://www.spigotmc.org/), so you'll need to include it as a dependency in your plugin's build configuration.

```xml
<!-- Maven -->
<dependency>
    <groupId>org.spigotmc</groupId>
    <artifactId>spigot-api</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```

Ensure that you replace the `version` tag's value with the version of the Spigot API that corresponds to your Minecraft server version.

## Contributing

If you'd like to contribute to the base-api project, feel free to fork the repository and submit a pull request with your changes. Contributions are always welcome!

## License

Base-API is released under the [GPLv3 License](LICENSE).

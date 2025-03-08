package com.deadlyweathercommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

@Mod(DeadlyWeatherCommands.MODID)
@EventBusSubscriber(modid = DeadlyWeatherCommands.MODID)
public class DeadlyWeatherCommands {
    public static final String MODID = "deadlyweathercommands";
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("deadlyweather-common.toml");

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        dispatcher.register(
            Commands.literal("deadlyweathercommands")
                .then(
                    Commands.literal("acidrain")
                        .then(
                            Commands.argument("action", StringArgumentType.word())
                                .executes(context ->
                                    toggleWeather(context.getSource(), "rainy", context.getArgument("action", String.class))
                                )
                        )
                )
                .then(
                    Commands.literal("sunny")
                        .then(
                            Commands.argument("action", StringArgumentType.word())
                                .executes(context ->
                                    toggleWeather(context.getSource(), "sunny", context.getArgument("action", String.class))
                                )
                        )
                )
                .then(
                    Commands.literal("thunder")
                        .then(
                            Commands.argument("action", StringArgumentType.word())
                                .executes(context ->
                                    toggleThunderWeather(context.getSource(), context.getArgument("action", String.class))
                                )
                        )
                )
                .then(
                    Commands.literal("snowy")
                        .then(
                            Commands.argument("action", StringArgumentType.word())
                                .executes(context ->
                                    toggleWeather(context.getSource(), "snowy", context.getArgument("action", String.class))
                                )
                        )
                )
        );
    }

    private static int toggleWeather(CommandSourceStack source, String section, String action) {
        if (!Files.exists(CONFIG_PATH)) {
            source.sendFailure(Component.literal("Configuration not found!"));
            return 0;
        }
        try {
            List<String> lines = Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8);
            List<String> newLines = new ArrayList<>();
            boolean inSection = false;
            boolean newValue = action.equals("enable");

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.equals("[" + section + "]")) {
                    inSection = true;
                } else if (trimmed.startsWith("[")) {
                    inSection = false;
                }
                if (inSection && trimmed.startsWith("enable = ")) {
                    newLines.add("enable = " + newValue);
                    continue;
                }
                newLines.add(line);
            }

            Files.write(CONFIG_PATH, newLines, StandardCharsets.UTF_8);
            String status = newValue ? "enabled" : "disabled";
            source.sendSuccess(() -> Component.literal("Weather " + section + " " + status), true);
            return Command.SINGLE_SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            source.sendFailure(Component.literal("Error modifying configuration!"));
            return 0;
        }
    }

    private static int toggleThunderWeather(CommandSourceStack source, String action) {
        if (action.equals("enable")) {
            toggleWeather(source, "thunder", "enable");
            toggleWeather(source, "thunder.playerSeeking", "enable");
        } else if (action.equals("disable")) {
            toggleWeather(source, "thunder", "disable");
            toggleWeather(source, "thunder.playerSeeking", "disable");
        } else {
            source.sendFailure(Component.literal("Unknown action! Use 'enable' or 'disable'."));
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }
}

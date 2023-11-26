package dev.plytki.baseapi.commands.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandInfo {

    String name();

    String description() default "No description provided.";

    String usage() default "";

    String permission() default "";

    String[] aliases() default {};

    Sender[] allowed() default { Sender.PLAYER, Sender.CONSOLE, Sender.REMOTE_CONSOLE };

}
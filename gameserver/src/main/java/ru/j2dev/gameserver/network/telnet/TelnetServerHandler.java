package ru.j2dev.gameserver.network.telnet;

import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.network.telnet.commands.*;
import ru.j2dev.gameserver.network.telnet.commands.TelnetServer;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelnetServerHandler extends SimpleChannelUpstreamHandler implements TelnetCommandHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelnetServerHandler.class);
    private static final Pattern COMMAND_ARGS_PATTERN = Pattern.compile("\"([^\"]*)\"|([^\\s]+)");

    private Set<TelnetCommand> _commands;

    public TelnetServerHandler() {
        (_commands = new LinkedHashSet<>()).add(new TelnetCommand("help", "h") {
            @Override
            public String getUsage() {
                return "help [command]";
            }

            @Override
            public String handle(final String[] args) {
                if (args.length == 0) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("Available commands:\n");
                    for (final TelnetCommand cmd : _commands) {
                        sb.append(cmd.getCommand()).append("\n");
                    }
                    return sb.toString();
                }
                final TelnetCommand cmd2 = TelnetServerHandler.this.getCommand(args[0]);
                if (cmd2 == null) {
                    return "Unknown command.\n";
                }
                return "usage:\n" + cmd2.getUsage() + "\n";
            }
        });
        addHandler(new TelnetBan());
        addHandler(new TelnetConfig());
        addHandler(new TelnetDebug());
        addHandler(new TelnetPerfomance());
        addHandler(new TelnetSay());
        addHandler(new TelnetServer());
        addHandler(new TelnetStatus());
        addHandler(new TelnetWorld());
    }

    public void addHandler(final TelnetCommandHolder handler) {
        _commands.addAll(handler.getCommands());
    }

    @Override
    public Set<TelnetCommand> getCommands() {
        return _commands;
    }

    private TelnetCommand getCommand(final String command) {
        for (final TelnetCommand cmd : _commands) {
            if (cmd.equals(command)) {
                return cmd;
            }
        }
        return null;
    }

    private String tryHandleCommand(final String command, final String[] args) {
        final TelnetCommand cmd = getCommand(command);
        if (cmd == null) {
            return "Unknown command.\n";
        }
        String response = cmd.handle(args);
        if (response == null) {
            response = "usage:\n" + cmd.getUsage() + "\n";
        }
        return response;
    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) {
        String sb = "Welcome to L2 GameServer telnet console.\n" +
                "It is " + new Date() + " now.\n";
        e.getChannel().write(sb.replaceAll("\n", "\r\n"));
        if (!Config.TELNET_PASSWORD.isEmpty()) {
            e.getChannel().write("Password:");
            ctx.setAttachment(Boolean.FALSE);
        } else {
            e.getChannel().write("Type 'help' to see all available commands.\r\n");
            ctx.setAttachment(Boolean.TRUE);
        }
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) {
        String request = (String) e.getMessage();
        String response = null;
        boolean close = false;
        if (Boolean.FALSE.equals(ctx.getAttachment())) {
            if (Config.TELNET_PASSWORD.equals(request)) {
                ctx.setAttachment(Boolean.TRUE);
                request = "";
            } else {
                response = "Wrong password!\n";
                close = true;
            }
        }
        if (Boolean.TRUE.equals(ctx.getAttachment())) {
            if (request.isEmpty()) {
                response = "Type 'help' to see all available commands: ";
            } else if ("exit".equals(request.toLowerCase())) {
                response = "Have a good day!\n";
                close = true;
            } else {
                final Matcher m = COMMAND_ARGS_PATTERN.matcher(request);
                m.find();
                final String command = m.group();
                final List<String> args = new ArrayList<>();
                while (m.find()) {
                    String arg = m.group(1);
                    if (arg == null) {
                        arg = m.group(0);
                    }
                    args.add(arg);
                }
                response = tryHandleCommand(command, args.toArray(new String[0]));
            }
        }
        final ChannelFuture future = e.getChannel().write(response.replaceAll("\n", "\r\n"));
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) {
        if (e.getCause() instanceof IOException) {
            e.getChannel().close();
        } else {
            LOGGER.error("", e.getCause());
        }
    }
}

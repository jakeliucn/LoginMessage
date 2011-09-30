package com.tahkeh.loginmessage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import com.tahkeh.loginmessage.entries.DefaultEntry;
import com.tahkeh.loginmessage.entries.Entry;
import com.tahkeh.loginmessage.entries.Group;
import com.tahkeh.loginmessage.entries.Op;
import com.tahkeh.loginmessage.entries.Permission;
import com.tahkeh.loginmessage.entries.Pri;
import com.tahkeh.loginmessage.entries.Pub;
import com.tahkeh.loginmessage.entries.User;
import com.tahkeh.loginmessage.store.Store;
import com.tahkeh.loginmessage.timers.Cooldown;
import com.tahkeh.loginmessage.timers.Delay;
import com.tahkeh.loginmessage.timers.Cooldown.CooldownTask;

import de.xzise.XLogger;
import de.xzise.wrappers.economy.EconomyHandler;
import de.xzise.wrappers.permissions.BufferPermission;
import de.xzise.wrappers.permissions.PermissionsHandler;

public class Message extends PlayerListener //Handles everything message-related (so 90% of this plugin)
{
	public final static char SECTION_SIGN = '\u00A7';
	private final static String[] EMPTY_STRING_ARRAY = new String[0];
	private final static BufferPermission<String> PREFIX_PERMISSION = BufferPermission.create("prefix", (String) null);
	private final static BufferPermission<String> SUFFIX_PERMISSION = BufferPermission.create("suffix", (String) null);

	private final Main plugin;
	private final Configuration config;
	private final Configuration message;
	private final XLogger logger;
	private final Store store;

	String separator = "%&%&"; //Currently a static string until I can figure out how to get YML to read characters as a string
	boolean cont = true;
	
	private final Cooldown cooldown;
	
	public Message(Main instance, Configuration config, Configuration message, XLogger logger, Store store)
	{
		this.plugin = instance;
		this.config = config;
		this.message = message;
		this.logger = logger;
		this.cooldown = new Cooldown();
		this.store = store;
		}

	public String getTimeDifference(long start) {
		Date end = Calendar.getInstance().getTime();
		long difference = (end.getTime() - start) / 1000;
		long date[] = new long[] {0, 0, 0, 0};
		StringBuilder sb = new StringBuilder();
		
		date[3] = (difference >= 60 ? difference % 60 : difference);
		date[2] = (difference = (difference / 60)) >= 60 ? difference % 60 : difference;
		date[1] = (difference = (difference / 60)) >= 24 ? difference % 24 : difference;
		date[0] = (difference = (difference / 24));
		
		if (date[0] > 0) {
			sb.append(String.format("%d day%s", date[0], date[0] != 1 ? "s" : ""));
		}
		if (date[1] > 0) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(String.format("%d hour%s", date[1], date[1] != 1 ? "s" : ""));
		}
		if (date[2] > 0) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(String.format("%d minute%s", date[2], date[2] != 1 ? "s" : ""));
		}
		if (date[3] > 0) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(String.format("%d second%s", date[3], date[3] != 1 ? "s" : ""));
		} else if (difference == 0) {
			sb.append("a moment");
		}
		return sb.toString();
	}
	
	/**
	 * Check the .dat files in the default world folder. If the given player is 
	 * found, return true.
	 * @param p
	 * 			the name of the player to look for
	 * @return whether or not player 'p' has joined the server before
	 */
	public boolean existingPlayer(String p) {
		String pdir = getDefaultWorld().getName() + File.separator + "players";
		File playerfile = new File(pdir);
		String[] playerfiles = playerfile.list();
		for(String player : playerfiles) {
			if(player.contains(p)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the first loaded, or default world.
	 * @return the default world
	 */
	public World getDefaultWorld() {
		  return plugin.getServer().getWorlds().get(0);
		}
	
	public String getLocation(String type, Player p, String event) {
		return store.getLocation(type, p, event);
	}
	
	public String getTime(Long rawtime, boolean caps) //Neat little method to get the text-based version of the time!
	{
		String day = config.getString("day");
		String dusk = config.getString("sunset");
		String dawn = config.getString("sunrise");
		String night = config.getString("night");

		int modTime = (int) (rawtime % 24000);

		String name = "";
		if (modTime == 24000 || modTime <= 11999) {
			name = day;
		} else if (modTime == 12000 || modTime <= 12999) {
			name = dusk;
		} else if (modTime == 13000 || modTime <= 22999) {
			name = night;
		} else if (modTime == 23000 || modTime <= 23999) {
			name = dawn;
		}
		return caps ? name : name.toLowerCase();
	}
	
	public String textProcess(String str) {
		boolean vowel = false;
		if(str.contains("%an%")) {
			String code = str.substring(str.indexOf("%an%"), str.indexOf("%an%") + 4);
			String letter = str.substring(str.indexOf("%an%") + 5, str.indexOf("%an%") + 6);
			if(letter.equalsIgnoreCase("a") || letter.equalsIgnoreCase("e") || letter.equalsIgnoreCase("i") || letter.equalsIgnoreCase("o") || letter.equalsIgnoreCase("u")) {
				vowel = true;
			}
			if(vowel) {
				str = str.replace(code, "an");
			} else {
				str = str.replace(code, "a");
			}
		}
		
		return str;
	}
	
	public String olProcess(String str, Player player, String event) //Method for processing %ol code
	{
		int on = 0;
		String list = "";
		String s = str;
        
        while(s.indexOf(separator) >= 0)
        {
        	s = s.substring(s.indexOf(separator) + 1);
        }
        Player[] online = plugin.getServer().getOnlinePlayers();
        int length = online.length - 1;
        List<Player> all_list = new ArrayList<Player>();
        for(Player all : online){
        		while(!all_list.contains(all)){
            		all_list.add(all);
            		//Create a list of all players that we can remove stuff from
            	}
        		if(isLeaveEvent(event)){
        			all_list.remove(player);
        			length = length - 1;
        		}
        }
        
       if(s.substring(separator.length() - 1, s.indexOf(":")).length() == 1 || s.substring(separator.length() - 1, s.indexOf(":")).length() == 0){
    	   PermissionsHandler handler = Main.getPermissions();
    	   //Credit to mathmaniac43 for awesome string trimming with substring()
    	   String a = s.substring(separator.length() - 1, s.indexOf(":"));
           s = s.substring(s.indexOf(":") + 1);

           String b = s.substring(0, s.indexOf(":"));
           s = s.substring(s.indexOf(":") + 1);

           String c = s.substring(0, 2);
           boolean sf1 = false;
           boolean pr1 = false;
           
           boolean sf2 = false;
           boolean pr2 = false;
           
           if(b.equalsIgnoreCase("sf")){
        	   sf1 = true;
        	   pr1 = false;
           } else if(b.equalsIgnoreCase("pr")){
        	   sf1 = false;
        	   pr1 = true;
           }
           if(c.equalsIgnoreCase("sf")){
        	   sf2 = true;
        	   pr2 = false;
           } else if(c.equalsIgnoreCase("pr")){
        	   sf2 = false;
        	   pr2 = true;
           }
    	       //TODO Find better way to get pr and sf working; this is too involved
    	    	   for(Player current : all_list){
    	    		   String b1 = "";
    	    		   String c1 = "";
    	    		   if(handler.isActive())
    	    		   {
    	    			   String world = current.getWorld().getName();
    	    			   String group = getFirst(handler.getGroup(world, current.getName()));
    	    			   b1 = sf1 && !pr1 ? getSuffix(group, world) : getPrefix(group, world);
        	    		   c1 = sf2 && !pr2 ? getSuffix(group, world) : getPrefix(group, world);
    	    		   }
    	    		   if(!sf1 && !pr1){
    	    			   b1 = b; //If we're using pr/sf, then the string will be b1, which is defined earlier
    	    		   }
    	    		   if(!sf2 && ! pr2){
    	    			   c1 = c; //Same deal as before
    	    		   }
    	    		   b1 = b1.replaceAll("(&([a-z0-9]))", SECTION_SIGN + "$2"); //My good buddy replace-ampersands-with-simoleon-symbols regex
    	    		   c1 = c1.replaceAll("(&([a-z0-9]))", SECTION_SIGN + "$2"); //When you think about it, it makes sense. $2 is the a-z/0-9 character
        	            	  if(a.equals("d")){
        	            		  list = list + (on >= length ? b1 + current.getDisplayName() : new StringBuilder().append(b1 + current.getDisplayName()).append(c1 + ", ").toString());
        	  	                  on++;
        	            	  } else {
        	            		  list = list + (on >= length ? b1 + current.getName() : new StringBuilder().append(b1 + current.getName()).append(c1 + ", ").toString());
        	  	                  on++;
        	            	  }
    	    	   }

    	       String ol = "%ol" + separator + a + ":" + b + ":" + c; //This is the string that people actually type in their messages
    	       str = str.replaceAll(ol, list); //Like any other code, replace this with the proper regex
       }
		return str;
	}
	
	private static <T> T getFirst(T[] array) {
		return getFirst(array, null);
	}
	
	private static <T> T getFirst(T[] array, T def) {
		if (array != null && array.length > 0) {
			return array[0];
		} else {
			return def;
		}
	}
	private static String getPrefix(String group, String world) {
		return Main.getPermissions().getString(world, group, PREFIX_PERMISSION);
	}
	
	private static String getSuffix(String group, String world) {
		return Main.getPermissions().getString(world, group, SUFFIX_PERMISSION);
	}
	
	public boolean isLeaveEvent(String event) //For %ol and %size
	{
		return event.equals("kick") || event.equals("quit");
	}
	
	public String process(String str, Player player, String event) {
		config.load();
		message.load();
		String list = "";
		Player[] online = plugin.getServer().getOnlinePlayers();
		int serverlist = online.length;
		if (isLeaveEvent(event)) {
			serverlist = serverlist - 1;
		}
		int servermax = plugin.getServer().getMaxPlayers();
		str = str.replaceAll("%dpnm", player.getDisplayName());
		str = str.replaceAll("%nm", player.getName());
		str = str.replaceAll("%size", Integer.toString(serverlist));
		str = str.replaceAll("%max", Integer.toString(servermax));
		if (str.contains("%ol" + separator)) {
			str = olProcess(str, player, event);
		} else if (str.contains("%ol")) {
			int on = 0;
			int length = serverlist - 1;
			List<Player> all_list = new ArrayList<Player>();
			for (Player all : online) {
				while (!all_list.contains(all)) {
					all_list.add(all);
				}
				if (isLeaveEvent(event)) {
					all_list.remove(player);
				}
			for (Player current : all_list) {
				list = list + (on >= length ? current.getName() : new StringBuilder().append(current.getName()).append(", ").toString());
		        on++;
		      }
			}
			str = str.replaceAll("%ol", list);
		}

		EconomyHandler economy = Main.getEconomy();
		if (economy.isActive()) {
			str = str.replaceAll("%bal", Double.toString(economy.getBalance(player.getName())));
		}
		PermissionsHandler permissions = Main.getPermissions();
		if (permissions.isActive()) {
			String groupname = getFirst(permissions.getGroup(player.getWorld().getName(),
					player.getName()));
			str = str.replaceAll("%group", groupname);

			String world = player.getWorld().getName();
			if (getPrefix(groupname, world) != null || getSuffix(groupname, world) != null) {
				String prefix = getPrefix(groupname, world);
				String suffix = getSuffix(groupname, world);

				if (prefix != null) {
					str = str.replaceAll("%prefix", prefix);
				}
				if (suffix != null) {
					str = str.replaceAll("%suffix", suffix);
				}

				StringBuilder permissionslist = new StringBuilder();
				int playerLength = online.length;
				for (Player current : online) {
					if (current != null) {
						String currentGroup = getFirst(permissions.getGroup(world, current.getName()));
						if (currentGroup != null) {
							String currentPrefix = getPrefix(currentGroup, world);
							String currentSuffix = getSuffix(currentGroup, world);
							permissionslist.append(currentPrefix).append(player.getName()).append(currentSuffix);
							if (playerLength > 1) {
								permissionslist.append(", ");
							}
						}
						playerLength--;
					}
				}
				str = str.replaceAll("%perol", permissionslist.toString());
			}
		}
		String ip = player.getAddress().getAddress().getHostAddress();
		if (!isLocal(player)) {
			str = str.replaceAll("%ip", ip);
		} else {
			str = str.replaceAll("%ip", Main.getExternalIp().getHostAddress());
		}
		if(str.contains("%city")) {
			str = str.replaceAll("%city", getLocation("city", player, event));
		}
		if(str.contains("%ccode")) {
			str = str.replaceAll("%ccode", getLocation("ccode", player, event));
		}
		if(str.contains("%cname")) {
			str = str.replaceAll("%cname", getLocation("cname", player, event));
		}
		if(str.contains("%zip")) {
			str = str.replaceAll("%zip", getLocation("zip", player, event));
		}
		if(str.contains("%rcode")) {
			str = str.replaceAll("%rcode", getLocation("rcode", player, event));
		}
		if(str.contains("%rname")) {
			str = str.replaceAll("%rname", getLocation("rname", player, event));
		}
		Long rawtime = player.getWorld().getTime();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(config.getString("format"));
		str = str.replaceAll("%world", player.getWorld().getName());
		str = str.replaceAll("%rtime", rawtime.toString());
		str = str.replaceAll("%time", getTime(rawtime, false));
		str = str.replaceAll("%Time", getTime(rawtime, true));
		str = str.replaceAll("%srtime", sdf.format(cal.getTime()));
		str = str.replaceAll("%laston", getTimeDifference(store.getLastLogin(player)));
		str = str.replaceAll("(&([a-z0-9]))", SECTION_SIGN + "$2");
		str = str.replaceAll("%sp", "");
		return textProcess(str);
	}

	public boolean isLocal(Player p) {
		return store.isLocal(p);
	}
	  
	
	public Set<Entry> getEntries(Player trigger, String key, String event, String type) //For receivers/triggers
	{
		Set<Entry> entries = new HashSet<Entry>();
		final String keypath = "messages." + event + "." + key + "." + type;
		final String userpath = keypath + ".users";
		final String grouppath = keypath + ".groups";
		final String permspath = keypath + ".permissions";
		for (String group : message.getStringList(grouppath, null)) {
			boolean positive = DefaultEntry.isPositive(group);
			String unsignedGroup = DefaultEntry.getUnsignedText(group);
			if (unsignedGroup.equalsIgnoreCase("pub")) {
				entries.add(new Pub(positive ? null : trigger));
			} else if (unsignedGroup.equalsIgnoreCase("op")) {
				entries.add(new Op(positive));
			} else if (unsignedGroup.equalsIgnoreCase("pri")) {
				entries.add(new Pri(positive, trigger));
			} else {
				entries.add(new Group(group, Main.getPermissions()));
			}
		}

		for(String user : message.getStringList(userpath, null))
		{
			entries.add(new User(user));
		}

		for (String perm : message.getStringList(permspath, null)) {
			entries.add(new Permission(perm, Main.getPermissions()));
		}
		return entries;
	}

	/**
	 * Prints all messages which the player is triggering.
	 * 
	 * @param trigger
	 *            the player who act as trigger.
	 * @param event
	 *            the event type (e.g. login, kick,...).
	 * @param cmdkey
	 *            the name of the message (for the commands). Will be ignored if
	 *            the event isn't <code>command</code>.
	 */
	public void preProcessMessage(Player trigger, String event, String cmdkey)
	{
		message.load();
		config.load();

		String[] messages;
		if (event.equals("command")) {
			messages = new String[] { cmdkey };
		} else {
			List<String> keyList = message.getKeys("messages." + event);
			if (keyList == null)   {
			  messages = EMPTY_STRING_ARRAY;
			} else {
			  messages = keyList.toArray(EMPTY_STRING_ARRAY);
			}
		}
		for (String key : messages) {
			Set<Entry> triggers = getEntries(trigger, key, event, "triggers");
			if (matchEntries(trigger, triggers)) {
				finishMessage(trigger, event, key);
			}
		}
	}

	private static boolean matchEntries(Player player, Collection<Entry> entries) {
		boolean match = false;
		for (Entry entry : entries) {
			if (entry.match(player)) {
				if (!entry.isPositive()) {
					return false;
				} else {
					match = true;
				}
			}
		}
		return match;
	}

	/**
	 * Returns the list of not empty message lines.
	 * @param event the event name of the message.
	 * @param name the name of the message.
	 * @return the list of not empty message lines.
	 */
	private String[] getLines(String event, String name) {
		List<ConfigurationNode> messages = this.message.getNodeList("messages." + event + "." + name + ".messages", null);
		String[] lines = EMPTY_STRING_ARRAY;
		if (messages != null && messages.size() > 0) {
			//See: MinecraftUtil.getRandomFromChances
			// Read chances
			double totalchance = 0;
			double defChance = 1.0 / messages.size();
			for (ConfigurationNode messageNode : messages) {
				totalchance += messageNode.getDouble("chance", defChance);
			}

			double value = Math.random() * totalchance;
			for (ConfigurationNode messageNode : messages) {
				value -= messageNode.getDouble("chance", defChance);
				if (value < 0) {
					lines = getStringList(messageNode, "message", EMPTY_STRING_ARRAY);
					break;
				}
			}
		} else {
			lines = getStringList(message, "messages." + event + "." + name + ".message", EMPTY_STRING_ARRAY);
		}
		List<String> cleanedLines = new ArrayList<String>(lines.length);
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] != null && !lines[i].isEmpty()) {
				cleanedLines.add(lines[i]);
			}
		}
		return cleanedLines.toArray(new String[cleanedLines.size()]);
	}

	/**
	 * Returns a string list from a yml configuration node. If the node points
	 * to a string list it returns the string list and if it is a string, a
	 * string splited by new lines.
	 * 
	 * @param node
	 *            yml node.
	 * @param path
	 *            path to the string.
	 * @param def
	 *            default value.
	 * @return a string list from a yml configuration node.
	 */
	public static String[] getStringList(ConfigurationNode node, String path, String[] def) {
		Object property = node.getProperty(path);
		if (property instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> rawList = (List<Object>) property;
			List<String> result = new ArrayList<String>(rawList.size());
			for (Object object : rawList) {
				if (object != null) {
					result.add(object.toString());
				}
			}
			return result.toArray(new String[result.size()]);
		} else if (property != null) {
			return property.toString().split("\\n");
		} else {
			return def;
		}
	}

	public void finishMessage(Player p, String event, String key) //Final touches - delay and cooldown
	{
		message.load();
		String[] lines = this.getLines(event, key);
		if (lines.length == 0) {
			this.logger.info("Empty message named '" + key + "' (Event: '" + event + "') found.");
		} else {
			final String keypath = "messages." + event + "." + key;
			int cd = message.getInt(keypath + ".cooldown", 0) * 1000;
			int dl = message.getInt(keypath + ".delay", 0);

			Player[] players = this.plugin.getServer().getOnlinePlayers();
			List<Player> cooledDown = new ArrayList<Player>(players.length);
			CooldownTask task = null;
			Set<Entry> receivers = getEntries(p, key, event, "receivers");

			// Since the only number lower than 1000 that is possible for the
			// cooldown is 0,
			// we make sure it isn't for the same reason as the delay.
			if (cd > 0) {
				List<String> cdstrs = new ArrayList<String>(players.length);
				for (Player player : players) {
					if (this.cooldown.isCooledDown(player, key, event) && matchEntries(player, receivers)) {
						cooledDown.add(player);
						cdstrs.add(Cooldown.createKey(player, key, event));
					}
				}
				task = this.cooldown.createTask(cdstrs, this.cooldown, cd);
			} else {
				for (Player player : players) {
					if (matchEntries(player, receivers)) {
						cooledDown.add(player);
					}
				}
				task = null;
			}

			if (cooledDown.size() > 0) {
				// Check if the delay isn't greater than or equal to 3.
				// Anything below 3 milliseconds makes your computer sad from my
				// experience.
				if (dl >= 3) {
					new Timer().schedule(new Delay(this, lines, p, event, cooledDown, task), dl);
				} else {
					sendMessage(p, cooledDown, lines, event, task);
				}
			}
		}
	}

	/**
	 * Sends the message to all receivers.
	 * 
	 * @param trigger
	 *            the player who triggered the message.
	 * @param possibleReceivers
	 *            the players who should receive the message if they are
	 *            receivers.
	 * @param key
	 *            the name of the message.
	 * @param event
	 *            the event type of the message (e.g. login).
	 */
	public void sendMessage(Player trigger, Collection<Player> possibleReceivers, String[] lines, String event, CooldownTask task) //The grand finale - get the right message, and if the player is a receiver, send it!
	{
		for (Player receiver : possibleReceivers) {
			for (String str : lines) {
				receiver.sendMessage(process(str, trigger, event));
			}
		}
		if (task != null) {
			task.trigger();
		}
	}
	
	public void onPlayerJoin(PlayerJoinEvent event) {
		config.load();
		Player p = event.getPlayer();
		String e = existingPlayer(p.getName()) ? "login" : "firstlogin";
		store.load(e);
		preProcessMessage(p, e, "");

		if (config.getBoolean("clearjoinmsg", true)) {
			event.setJoinMessage(null);
		}
	}	

	public void onPlayerQuit(PlayerQuitEvent event) {
		if(cont) {
			store.load("quit");
			config.load();
			Player p = event.getPlayer();
			preProcessMessage(p, "quit", "");

			if (config.getBoolean("clearquitmsg", true)) {
				event.setQuitMessage(null);
			}
		} else {
			cont = true;
		}
	}

	public void onPlayerKick(PlayerKickEvent event) {
		cont = false;
		store.load("kick");
		config.load();
		Player p = event.getPlayer();
		preProcessMessage(p, "kick", "");

		if (config.getBoolean("clearkickmsg", true)) {
			event.setLeaveMessage(null);
		}
	}

	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		store.load("command");
		message.load();
		Player p = event.getPlayer();
		String msg = event.getMessage();
		String cmd = msg.substring(1); // This is the bare command, without "/"
		List<String> commands = message.getKeys("messages.command");
		if(commands != null) {
			for (String key : commands) {
				if (key != null && msg.equalsIgnoreCase("/" + key)) {
					event.setCancelled(true);
					preProcessMessage(p, "command", cmd);
					break;
				}
			}
		}
	}
}
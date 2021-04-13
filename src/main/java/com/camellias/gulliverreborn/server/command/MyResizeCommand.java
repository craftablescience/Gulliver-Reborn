package com.camellias.gulliverreborn.server.command;

import java.util.List;
import java.util.UUID;

import com.artemis.artemislib.util.attributes.ArtemisLibAttributes;
import com.camellias.gulliverreborn.config.GulliverRebornConfig;
import com.camellias.gulliverreborn.GulliverReborn;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

public class MyResizeCommand extends CommandBase {
	private static final List<String> aliases = Lists.newArrayList(GulliverReborn.MODID, "mysize", "ms");
	private static final UUID uuidHeight = UUID.fromString("5440b01a-974f-4495-bb9a-c7c87424bca4");
	private static final UUID uuidWidth = UUID.fromString("3949d2ed-b6cc-4330-9c13-98777f48ea51");
	private static final UUID uuidReach1 = UUID.fromString("854e0004-c218-406c-a9e2-590f1846d80b");
	private static final UUID uuidReach2 = UUID.fromString("216080dc-22d3-4eff-a730-190ec0210d5c");
	private static final UUID uuidHealth = UUID.fromString("3b901d47-2d30-495c-be45-f0091c0f6fb2");
	private static final UUID uuidStrength = UUID.fromString("558f55be-b277-4091-ae9b-056c7bc96e84");
	private static final UUID uuidSpeed = UUID.fromString("f2fb5cda-3fbe-4509-a0af-4fc994e6aeca");
	
	@Override
	public @Nonnull String getName() {
		return "mysize";
	}

	@Override
	public @Nonnull String getUsage(@Nonnull ICommandSender sender) {
		return "gulliverreborn.commands.mysize.usage";
	}
	
	@Override
	public @Nonnull List<String> getAliases() {
		return aliases;
	}
	
	@Override
	public boolean checkPermission(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender) {
		return true;
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return GulliverRebornConfig.COMMAND_MYRESIZE_PERMISSION_LEVEL;
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args) {
		if (args.length < 1) return;

		String s = args[0];
		float size;

		try {
			size = MathHelper.clamp(Float.parseFloat(s), 0.01f, 30f);
		} catch (NumberFormatException e) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "Size Invalid"));
			return;
		}

		if (sender instanceof EntityPlayer) {
			size = MathHelper.clamp(size, 0.125F, GulliverRebornConfig.MAX_SIZE);
			Multimap<String, AttributeModifier> attributes = HashMultimap.create();
			Multimap<String, AttributeModifier> removeableAttributes = HashMultimap.create();
			Multimap<String, AttributeModifier> removeableAttributes2 = HashMultimap.create();

			attributes.put(ArtemisLibAttributes.ENTITY_HEIGHT.getName(), new AttributeModifier(uuidHeight, "Player Height", size - 1, 2));
			attributes.put(ArtemisLibAttributes.ENTITY_WIDTH.getName(), new AttributeModifier(uuidWidth, "Player Width", MathHelper.clamp(size - 1, 0.4 - 1, GulliverRebornConfig.MAX_SIZE), 2));

			if (GulliverRebornConfig.SPEED_MODIFIER) attributes.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(uuidSpeed, "Player Speed", (size - 1) / 2, 2));
			if (GulliverRebornConfig.REACH_MODIFIER) removeableAttributes.put(EntityPlayer.REACH_DISTANCE.getName(), new AttributeModifier(uuidReach1, "Player Reach 1", size - 1, 2));
			if (GulliverRebornConfig.REACH_MODIFIER) removeableAttributes2.put(EntityPlayer.REACH_DISTANCE.getName(), new AttributeModifier(uuidReach2, "Player Reach 2", -MathHelper.clamp(size - 1, 0.33, Double.MAX_VALUE), 2));
			if (GulliverRebornConfig.STRENGTH_MODIFIER) attributes.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(uuidStrength, "Player Strength", size - 1, 0));
			if (GulliverRebornConfig.HEALTH_MODIFIER) attributes.put(SharedMonsterAttributes.MAX_HEALTH.getName(), new AttributeModifier(uuidHealth, "Player Health", (size - 1) * GulliverRebornConfig.HEALTH_MULTIPLIER, 2));

			if (size > 1)
				((EntityPlayer) sender).getAttributeMap().applyAttributeModifiers(removeableAttributes);
			else
				((EntityPlayer) sender).getAttributeMap().removeAttributeModifiers(removeableAttributes);

			if (size < 1)
				((EntityPlayer) sender).getAttributeMap().applyAttributeModifiers(removeableAttributes2);
			else
				((EntityPlayer) sender).getAttributeMap().removeAttributeModifiers(removeableAttributes2);

			((EntityPlayer) sender).getAttributeMap().applyAttributeModifiers(attributes);
			((EntityPlayer) sender).setHealth(((EntityPlayer) sender).getMaxHealth());

			GulliverReborn.LOGGER.info(((EntityPlayer) sender).getDisplayNameString() + " set their size to " + size);
		} else if (sender instanceof TileEntity)
			GulliverReborn.LOGGER.info("Cannot run /mysize command inside nonplayer! Ran at " + ((TileEntity) sender).getPos());
	}
}
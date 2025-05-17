// 文件路径：src/main/java/com/example/cheatmod/CheatModClient.java
package com.example.cheatmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CheatModClient implements ClientModInitializer {
	private static final int DURATION = -1; // 30秒
	private static final KeyBinding KEY_BINDING = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.cheatmod.activate",
					InputUtil.Type.KEYSYM,
					GLFW.GLFW_KEY_Z,
					"category.cheatmod"
			));

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (KEY_BINDING.wasPressed()) {
				handleEffects(client);
			}
		});
	}

	private void handleEffects(MinecraftClient client) {
		if (client.player == null) return;

		if (client.isInSingleplayer()) {
			applySingleplayerEffects(client);
		} else {
			client.player.sendMessage(Text.literal("§c该模组禁止在服务器使用！"), false);
		}
	}

	private void applySingleplayerEffects(MinecraftClient client) {
		IntegratedServer server = client.getServer();
		if (server == null) return;

		server.execute(() -> { // 在服务端线程执行
			var serverPlayer = server.getPlayerManager().getPlayer(client.player.getUuid());
			if (serverPlayer == null) return;

			// 在服务端一次性添加所有效果
			applyEffects(serverPlayer);

			// 同步到客户端（只需要发送消息）
			client.execute(() -> {
				client.player.sendMessage(Text.literal("§a作弊效果已激活！"), false);
			});
		});
	}

	private void applyEffects(net.minecraft.server.network.ServerPlayerEntity player) {
		player.addStatusEffect(new StatusEffectInstance(
				StatusEffects.RESISTANCE, DURATION, 255, false, false, true));
		player.addStatusEffect(new StatusEffectInstance(
				StatusEffects.REGENERATION, DURATION, 255, false, false, true));
		player.addStatusEffect(new StatusEffectInstance(
				StatusEffects.STRENGTH, DURATION, 255, false, false, true));
	}
}
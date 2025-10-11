package dev.kikugie.techutils.feature.containerscan.verifier;

import dev.kikugie.techutils.util.ItemPredicateUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ItemPredicateEntryScreen extends Screen {
	private static final Text TITLE = Text.translatable("item_predicate_entry_screen.title");
	private static final Text INPUT_TEXT = Text.translatable("item_predicate_entry_screen.input");
	private final ClientPlayerEntity player;
	private ItemStack placeholder;
	private String initInput;
	protected TextFieldWidget consoleCommandTextField;
	protected ButtonWidget doneButton;
	protected ButtonWidget cancelButton;

	public ItemPredicateEntryScreen(ClientPlayerEntity player) {
		super(NarratorManager.EMPTY);
		this.player = player;
	}

	public ItemPredicateEntryScreen(ClientPlayerEntity player, ItemStack placeholder) {
		this(player);
		this.placeholder = placeholder;
	}

	public ItemPredicateEntryScreen(ClientPlayerEntity player, String input) {
		this(player);
		this.initInput = input;
	}

	public ItemPredicateEntryScreen(ClientPlayerEntity player, String input, ItemStack placeholder) {
		this(player, input);
		this.placeholder = placeholder;
	}

	protected void commitAndClose() {
		var stack = ItemPredicateUtils.createPredicateStack(consoleCommandTextField.getText(), placeholder);


		int selectedSlot = player.getInventory().getSelectedSlot();
		player.getInventory().setStack(selectedSlot, stack);
		this.client.interactionManager.clickCreativeStack(stack, 36 + selectedSlot);
		this.player.playerScreenHandler.sendContentUpdates();

		this.client.setScreen(null);
	}

	@Override
	protected void init() {
		this.doneButton = this.addDrawableChild(
			ButtonWidget.builder(ScreenTexts.DONE, button -> this.commitAndClose()).dimensions(this.width / 2 - 4 - 150, this.height / 16 + 120 + 12, 150, 20).build()
		);
		this.cancelButton = this.addDrawableChild(
			ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.close()).dimensions(this.width / 2 + 4, this.height / 16 + 120 + 12, 150, 20).build()
		);
		this.consoleCommandTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, 50, 300, 20, Text.translatable("advMode.command"));
		this.consoleCommandTextField.setMaxLength(100000);
		this.consoleCommandTextField.setText(initInput);
		this.addSelectableChild(this.consoleCommandTextField);
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.consoleCommandTextField);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.consoleCommandTextField.getText();
		this.init(client, width, height);
		this.consoleCommandTextField.setText(string);
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		if (super.keyPressed(input)) {
			return true;
		} else if (input.key() != GLFW.GLFW_KEY_ENTER && input.key() != GLFW.GLFW_KEY_KP_ENTER) {
			return false;
		} else {
			this.commitAndClose();
			return true;
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, TITLE, this.width / 2, 20, 16777215);
		context.drawTextWithShadow(this.textRenderer, INPUT_TEXT, this.width / 2 - 150 + 1, 40, 10526880);
		this.consoleCommandTextField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}
}

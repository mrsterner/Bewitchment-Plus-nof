package dev.mrsterner.bewitchmentplus.common.entity;

import com.google.common.collect.Maps;
import dev.mrsterner.bewitchmentplus.BewitchmentPlus;
import dev.mrsterner.bewitchmentplus.common.registry.BWPLootTables;
import dev.mrsterner.bewitchmentplus.common.registry.BWPObjects;
import dev.mrsterner.bewitchmentplus.mixin.MobEntityAccessor;
import moriyashiine.bewitchment.api.BewitchmentAPI;
import moriyashiine.bewitchment.common.entity.living.LeonardEntity;
import moriyashiine.bewitchment.common.entity.living.util.BWHostileEntity;
import moriyashiine.bewitchment.common.registry.BWObjects;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.gen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class CambionEntity extends BWHostileEntity implements InventoryChangedListener, InventoryOwner {
	private static final TrackedData<Boolean> BABY = DataTracker.registerData(PiglinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static final TrackedData<Boolean> MALE = DataTracker.registerData(CambionEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	public SimpleInventory cambionInventory = new SimpleInventory(8);
	private static final Map<EquipmentSlot, Identifier> EQUIPMENT_SLOT_ITEMS = Util.make(Maps.newHashMap(),
	(slotItems) -> {
		slotItems.put(EquipmentSlot.MAINHAND, BWPLootTables.CAMBION_MAIN_HAND);
		slotItems.put(EquipmentSlot.OFFHAND, BWPLootTables.CAMBION_OFF_HAND);
		slotItems.put(EquipmentSlot.HEAD, BWPLootTables.CAMBION_HELMET);
		slotItems.put(EquipmentSlot.CHEST, BWPLootTables.CAMBION_CHESTPLATE);
		slotItems.put(EquipmentSlot.LEGS, BWPLootTables.CAMBION_LEGGINGS);
		slotItems.put(EquipmentSlot.FEET, BWPLootTables.CAMBION_BOOTS);
	});


	public CambionEntity(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
		this.cambionInventory.addListener(this);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
		.add(EntityAttributes.GENERIC_MAX_HEALTH, 20.00D)
		.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0D)
		.add(EntityAttributes.GENERIC_ARMOR, 2.0D)
		.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35D)
		.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.35D)
		.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0D);
	}

	public EntityGroup getGroup() {
		return BewitchmentAPI.DEMON;
	}

	protected boolean hasShiny() {
		return true;
	}

	@Override
	public boolean isLeftHanded() {
		return false;
	}

	@Override
	public boolean canUsePortals() {
		return true;
	}

	@Override
	public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
		boolean flag = super.canSpawn(world, spawnReason);
		if (flag && (spawnReason == SpawnReason.SPAWNER || spawnReason == SpawnReason.STRUCTURE || spawnReason == SpawnReason.MOB_SUMMONED || spawnReason == SpawnReason.SPAWN_EGG || spawnReason == SpawnReason.COMMAND || spawnReason == SpawnReason.DISPENSER || spawnReason == SpawnReason.NATURAL)) {
			return true;
		}
		if (world instanceof ServerWorld && BewitchmentPlus.config.cambionVillageStructureSpawn) {
			BlockPos nearestVillage = ((ServerWorld) world).locateStructure(StructureFeature.VILLAGE, getBlockPos(), 3, false);
			return (nearestVillage != null && Math.sqrt(nearestVillage.getSquaredDistance(getBlockPos())) < 128);
		}
		if (world instanceof ServerWorld && BewitchmentPlus.config.cambionNetherFortressStructureSpawn) {
			BlockPos nearestFortress = ((ServerWorld) world).locateStructure(StructureFeature.FORTRESS, getBlockPos(), 3, false);
			return (nearestFortress != null && Math.sqrt(nearestFortress.getSquaredDistance(getBlockPos())) < 128);
		}
		if (world instanceof ServerWorld && BewitchmentPlus.config.cambionBastionStructureSpawn) {
			BlockPos nearestBastion = ((ServerWorld) world).locateStructure(StructureFeature.BASTION_REMNANT, getBlockPos(), 3, false);
			return (nearestBastion != null && Math.sqrt(nearestBastion.getSquaredDistance(getBlockPos())) < 128);
		}
		return false;
	}

	@Override
	public void equipStack(EquipmentSlot slotIn, ItemStack stack) {
		super.equipStack(slotIn, stack);
		switch (slotIn) {
			case CHEST:
				if (this.cambionInventory.getStack(1).isEmpty())
					this.cambionInventory.setStack(1, ((MobEntityAccessor)this).armorItems().get(slotIn.getEntitySlotId()));
				break;
			case FEET:
				if (this.cambionInventory.getStack(3).isEmpty())
					this.cambionInventory.setStack(3, ((MobEntityAccessor)this).armorItems().get(slotIn.getEntitySlotId()));
				break;
			case HEAD:
				if (this.cambionInventory.getStack(0).isEmpty())
					this.cambionInventory.setStack(0, ((MobEntityAccessor)this).armorItems().get(slotIn.getEntitySlotId()));
				break;
			case LEGS:
				if (this.cambionInventory.getStack(2).isEmpty())
					this.cambionInventory.setStack(2, ((MobEntityAccessor)this).armorItems().get(slotIn.getEntitySlotId()));
				break;
			case MAINHAND:
				if (this.cambionInventory.getStack(5).isEmpty())
					this.cambionInventory.setStack(5, ((MobEntityAccessor)this).armorItems().get(slotIn.getEntitySlotId()));
				break;
			case OFFHAND:
				if (this.cambionInventory.getStack(4).isEmpty())
					this.cambionInventory.setStack(4, ((MobEntityAccessor)this).armorItems().get(slotIn.getEntitySlotId()));
				break;
		}
	}


	public ItemStack getPickedResult(HitResult target) {
		return new ItemStack(BWPObjects.CAMBION_SPAWN_EGG.asItem());
	}

	@Override
	public void tickMovement() {
		this.tickHandSwing();
		super.tickMovement();
	}

	@Override
	public boolean tryAttack(Entity target) {
		ItemStack hand = this.getMainHandStack();
		hand.damage(1, this, (entity) -> entity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
		return super.tryAttack(target);
	}

	//Todo: Redo this to mirror logic used on 1.12.2. It was handled better there.
	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityTag) {
		this.setPersistent();
		this.initEquipment(difficulty);

		dataTracker.set(MALE, random.nextBoolean());
		Random rand = new Random();
		int a = rand.nextInt(4);
		int c = rand.nextInt(4);
		if (a == 3) {
			this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		}
		if (a == 2) {
			this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
		}
		if (a == 1) {
			this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(BWObjects.ATHAME));
		}
		if (c == 1) {
			this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
		}
		return super.initialize(world, difficulty, spawnReason, entityData, entityTag);
	}

	@Override
	protected void initEquipment(LocalDifficulty difficulty) {
		for (EquipmentSlot equipmentslottype : EquipmentSlot.values()) {
			for (ItemStack stack : this.getItemsFromLootTable(equipmentslottype)) {
				this.equipStack(equipmentslottype, stack);
			}
		}
		this.handDropChances[EquipmentSlot.MAINHAND.getEntitySlotId()] = 100.0F;
		this.handDropChances[EquipmentSlot.OFFHAND.getEntitySlotId()] = 100.0F;
	}

	public static int slotToInventoryIndex(EquipmentSlot slot) {
		return switch (slot) {
			case CHEST -> 1;
			case LEGS -> 2;
			case FEET -> 3;
			default -> 0;
		};
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound tag) {
		super.readCustomDataFromNbt(tag);
		this.setBaby(tag.getBoolean("IsBaby"));
		dataTracker.set(MALE, tag.getBoolean("Male"));
		NbtList listnbt = tag.getList("Inventory", 10);
		for (int i = 0; i < listnbt.size(); ++i) {
			NbtCompound compoundnbt = listnbt.getCompound(i);
			int j = compoundnbt.getByte("Slot") & 255;
			this.cambionInventory.setStack(j, ItemStack.fromNbt(compoundnbt));
		}
		if (tag.contains("ArmorItems", 9)) {
			NbtList armorItems = tag.getList("ArmorItems", 10);
			for (int i = 0; i < ((MobEntityAccessor)this).armorItems().size(); ++i) {
				int index = CambionEntity.slotToInventoryIndex(MobEntity.getPreferredEquipmentSlot(ItemStack.fromNbt(armorItems.getCompound(i))));
				this.cambionInventory.setStack(index, ItemStack.fromNbt(armorItems.getCompound(i)));
			}
		}
		if (tag.contains("HandItems", 9)) {
			NbtList handItems = tag.getList("HandItems", 10);
			for (int i = 0; i < ((MobEntityAccessor)this).handItems().size(); ++i) {
				int handSlot = i == 0 ? 5 : 4;
				this.cambionInventory.setStack(handSlot, ItemStack.fromNbt(handItems.getCompound(i)));
			}
		}
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound tag) {
		super.writeCustomDataToNbt(tag);
		tag.putBoolean("Male", dataTracker.get(MALE));
		if (this.isBaby()) {
			tag.putBoolean("IsBaby", true);
		}
		NbtList listnbt = new NbtList();
		for (int i = 0; i < this.cambionInventory.size(); ++i) {
			ItemStack itemstack = this.cambionInventory.getStack(i);
			NbtCompound compoundnbt = new NbtCompound();
			compoundnbt.putByte("Slot", (byte) i);
			itemstack.writeNbt(compoundnbt);
			listnbt.add(compoundnbt);

		}
		tag.put("Inventory", listnbt);
	}

	@Override
	protected void damageArmor(DamageSource damageSource, float damage) {
		if (damage >= 0.0F) {
			damage = damage / 4.0F;
			if (damage < 1.0F) {
				damage = 1.0F;
			}
			for (int i = 0; i < this.cambionInventory.size(); ++i) {
				ItemStack itemstack = this.cambionInventory.getStack(i);
				if ((!damageSource.isFire() || !itemstack.getItem().isFireproof())
				&& itemstack.getItem() instanceof ArmorItem) {
					int j = i;
					itemstack.damage((int) damage, this, (p_214023_1_) -> {
						p_214023_1_.sendEquipmentBreakStatus(EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, j));
					});
				}
			}
		}
	}



	protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
		super.dropEquipment(source, lootingMultiplier, allowDrops);
		this.cambionInventory.clearToList().forEach(this::dropStack);
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		dataTracker.startTracking(MALE, true);
		this.dataTracker.startTracking(BABY, false);
	}

	public List<ItemStack> getItemsFromLootTable(EquipmentSlot slot) {
		if (EQUIPMENT_SLOT_ITEMS.containsKey(slot)) {
			LootTable loot = this.world.getServer().getLootManager().getTable(EQUIPMENT_SLOT_ITEMS.get(slot));
			LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld) this.world)).parameter(LootContextParameters.THIS_ENTITY, this).random(this.getRandom());
			return loot.generateLoot(lootcontext$builder.build(BWPLootTables.SLOT));
		}
		return null;
	}

	@Override
	protected void initGoals() {
		goalSelector.add(0, new SwimGoal(this));
		goalSelector.add(1, new MeleeAttackGoal(this, 1, true));
		goalSelector.add(2, new WanderAroundFarGoal(this, 1));
		goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8));
		goalSelector.add(3, new LookAroundGoal(this));
		targetSelector.add(0, new RevengeGoal(this));
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, MobEntity.class, 5, false, false, (livingEntity) -> livingEntity instanceof Monster && !(livingEntity instanceof CambionEntity) && !(livingEntity instanceof LeonardEntity)));
	}

	@Override
	public boolean canPickupItem(ItemStack stack) {
		return super.canPickupItem(stack);
	}



	@Override
	protected boolean shouldDropLoot() {
		return super.shouldDropLoot();
	}

	@Override
	public int getVariants() {
		return 3;
	}

	@Override
	public void onInventoryChanged(Inventory sender) {

	}

	@Override
	public Inventory getInventory() {
		return cambionInventory;
	}

	@Override
	public boolean isBaby() {
		return this.getDataTracker().get(BABY);
	}


}

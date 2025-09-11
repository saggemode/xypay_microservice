# UUID to Long Conversion Fix Guide

## Step 1: Remove UUID imports from domain entities
Find and remove all `import java.util.UUID;` lines from domain entities since they now inherit Long id from BaseEntity.

## Step 2: Update Repository interfaces
Change all repository interfaces from:
```java
public interface SomeRepository extends JpaRepository<SomeEntity, UUID>
```
to:
```java
public interface SomeRepository extends JpaRepository<SomeEntity, Long>
```

## Step 3: Update Service method signatures
Change method parameters from `UUID id` to `Long id` in all service classes.

## Step 4: Update DTOs
Change DTO fields from `private UUID id` to `private Long id` and update getters/setters.

## Step 5: Update Controllers and other classes
Update any remaining UUID references to Long.

## Files that need immediate attention:
- CustomerEscalation.java (remove UUID references, fix onCreate() method)
- All repository interfaces
- All service classes with UUID parameters
- DTO classes and mappers
- Controllers with UUID parameters

## Quick fixes for critical errors:
1. Remove `import java.util.UUID;` from all domain entities
2. Remove custom getId()/setId() methods from entities (inherited from BaseEntity)
3. Fix repository generic types
4. Update service method signatures

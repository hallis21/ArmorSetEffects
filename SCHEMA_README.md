# ArmorSetEffects JSON Schema

The JSON Schema for ArmorSetEffects V2 armor set configuration files is automatically created in the `plugins/ArmorSetEffects/sets/` directory.

## Files

- `plugins/ArmorSetEffects/sets/armor-sets-schema.json` - The complete JSON Schema definition
- Schema is automatically copied from plugin resources when the plugin first runs

## Usage

### IDE Integration

Most modern IDEs support JSON Schema validation. To enable schema validation in your armor set files:

#### VS Code
Add this to the top of your armor set JSON files:
```json
{
  "$schema": "../armor-sets-schema.json",
  "your_armor_set": {
    // ... armor set definition
  }
}
```

Or configure in your VS Code settings.json:
```json
{
  "json.schemas": [
    {
      "fileMatch": ["**/sets/**/*.json"],
      "url": "./sets/armor-sets-schema.json"
    }
  ]
}
```

#### IntelliJ IDEA / WebStorm
Go to Settings → Languages & Frameworks → Schemas and DTDs → JSON Schema Mappings and add:
- Schema file: `armor-sets-schema.json`
- File pattern: `**/sets/**/*.json`

### Command Line Validation

You can validate your armor set files using various tools:

#### Using ajv-cli (Node.js)
```bash
npm install -g ajv-cli
ajv validate -s plugins/ArmorSetEffects/sets/armor-sets-schema.json -d "plugins/ArmorSetEffects/sets/**/*.json"
```

#### Using jsonschema (Python)
```bash
pip install jsonschema
python -c "
import json
import jsonschema
schema = json.load(open('plugins/ArmorSetEffects/sets/armor-sets-schema.json'))
data = json.load(open('plugins/ArmorSetEffects/sets/examples/diamond_set.json'))
jsonschema.validate(data, schema)
print('Valid!')
"
```

## Schema Features

The schema provides:

- **Complete type validation** for all armor set properties
- **Enum validation** for effect types and armor materials
- **Required field validation** with helpful error messages
- **Property constraints** (min/max values, string patterns)
- **Comprehensive documentation** for each field

## Supported Values

### Armor Materials
All standard Minecraft armor types are supported:
- Leather, Chainmail, Iron, Golden, Diamond, Netherite
- Special items: Turtle Helmet, Elytra

### Effect Types
All current Minecraft potion effects including new 1.21+ effects:
- Standard effects: SPEED, STRENGTH, REGENERATION, etc.
- Protection effects: FIRE_RESISTANCE, WATER_BREATHING, etc.
- New effects: DARKNESS, TRIAL_OMEN, WIND_CHARGED, etc.

### Message Formatting
All message fields support MiniMessage formatting:
- `<green>Text</green>` - Colored text  
- `<bold>Text</bold>` - Bold text
- `<gradient:red:blue>Text</gradient>` - Gradient colors
- And many more MiniMessage features

## Example Usage

See `plugins/ArmorSetEffects/sets/examples/diamond_set.json` for a complete example that includes the schema reference.

## Validation Errors

Common validation errors and fixes:

- **Invalid effect type**: Check spelling against the enum list in the schema
- **Invalid material**: Ensure armor material names are exact (e.g., `DIAMOND_HELMET`)
- **Missing required fields**: `name` is required, `item` and `slot` required for armor pieces
- **Type mismatches**: Ensure numbers are integers, booleans are true/false, etc.
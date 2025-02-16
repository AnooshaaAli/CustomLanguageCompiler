# Ohio Programming Language
## Syntax and Rules

### 1. **Data Types**
Ohio supports the following data types:

- `alpha` - Integer values (e.g., `int age = 25;`)
- `beta` - Decimal (floating-point) values (e.g., `dec pi = 3.14159;`)
- `rizz` - Boolean values (`true` or `false`)
- `gamma` - Single character values (e.g., `char letter = 'A';`)

### 2. **Variable Declaration**
- Variables must be declared before use.
- Only lowercase letters are allowed for variable names.
- Example:
  ```ohio
  alpha x = 10;
  beta pi = 3.14;
  rizz is_valid = true;
  ```

### 3. **Constants**
- Global constants must be declared outside `code {}`.
  - Example:
    ```ohio
    << a global variable >>
    beta pi = 3.14159;
    
    code {
      << a local varible >>
      beta radius = 10.0;
    }
    ```

### 4. **Operators**
- Arithmetic: `+`, `-`, `*`, `/`, `%`
- Comparison: `==`, `!=`, `>`, `<`, `>=`, `<=`
- Assignment: `=`

### 5. **Control Structures**
#### **If-Else Statement**
```ohio
flip () {
    << true case code >>
} twist() {
    << else if case code >>
} flop() {
    << false case code >>
}
```
Example:
```ohio
flip (x > 0) {
    echo("x is positive");
} twist (x < 0) {
    echo("x is negative");
} flop {
    echo("x is 0");
}

```

#### **Loops**
- **While Loop:**
```ohio
spin (condition) {
    << Code block >>
}
```
Example:
```ohio
alpha i = 0;
spin (i < 5) {
    echo(i);
    i = i + 1;
}
```

### 6. **Input/Output**
- **Printing to Console:**
```ohio
echo("Hello, World!");
```
- **User Input:**
```ohio
alpha age = capture("Enter your age: ");
```

### 7. **Comments**
- **Single-line comment:** `<< This is a comment >>`
- **Multi-line comment:**
```ohio
<<<
   This is a multi-line comment.
>>>
```

### 8. **Functions**
- Functions must be declared before use.
- Example:
```ohio
func add(int a, int b) {
    return a + b;
}
alpha result = add(5, 3);
echo("Sum:", result);
```

### 9. **Code Blocks & Indentation**
- Main block of code is enclosed in `code{}`.
- Proper indentation is recommended but not required.

## Example Program
```ohio
<< Calculate area of a circle >>

beta pi = 3.14159;

code {
    alpha radius = capture("Radius: ");
    beta area = pi * radius * radius;
    
    flip (area > 50) {
        echo("Large circle");
    } flop {
        echo("Small circle");
    }
}
```

## File Extension
- Ohio files should use the `.ohio` extension.

## End of File (EOF)
- The program ends automatically at the last line unless explicitly stated.


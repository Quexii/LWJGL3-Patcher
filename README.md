# LWJGL Patcher
All this tool does is essentially just rename 2 packages from `org.lwjgl` to `org.lwjgl3`.
This is only useful if you for some reason need to use some features of LWJGL3 in LWJGL2, such as NanoVG or TinyFD.

## Usage
1. Close the repository
2. Open [TestMain.kt](src/test/kotlin/TestMain.kt)
3. Select what LWJGL3 modules you would like to include in the patched JAR
4. Run the `main` function

## Notes
As to what modules are available, check the [LWJGL3](https://www.lwjgl.org/customize) page.
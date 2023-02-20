# TyporaImageHelper
Make the image reference in md files match the real image path by moving image file or modify image reference
When you sort your directory structure of your md files, you may find that the image reference in your md files is not correct.
And you have to move the image file to the correct dir or modify the image reference in your md files manually.
This tool can help you to do this automatically.

# Usage
```shell
# java -jar main.jar <image path> <rule>
# rule: 0 - move image file to the same dir as the relevant md file stored
# rule: 1 - move image file to the parent dir of the dir where relevant md file stored
# rule: 2 - move the image file to a specified dir
java -jar main.jar **/NutStore/note 0 
java -jar main.jar **/NutStore/note 1
java -jar main.jar **/NutStore/note 2 /Users/xxx/Pictures
```
suggest you to init a git repo in the dir where you store your md files, 
and it will help you to track the changes of your md files

Hope you try it and give me some feedback!
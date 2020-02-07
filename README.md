# 编译技术概览
编译器技术里分为前端技术和后端技术。

在这个项目中，我们只会涉及前端技术。

前端技术大致分为三步：
1. 词法分析：程序分割成一个个 Token 的过程，可以通过构造有限自动机来实现
2. 语法分析：把程序的结构识别出来，并形成一棵便于由计算机处理的抽象语法树。可以用递归下降的算法来实现
3. 语义分析：消除语义模糊，生成一些属性信息，让计算机能够依据这些信息生成目标代码
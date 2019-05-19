# 选文查词

制作的缘由，最近下载了几个学英语用的app，遇到生词总是要复制，然后粘贴到某个地方去查询，步骤很麻烦。有时候，查完的单词，还想以后认得的话，那就得加到平时使用的背单词软件的生词本里面去。不如写个软件给自己用吧！于是，这个软件就在某天晚上开搞了。基本的思路是：选择一个单词，复制后，显示这个单词的音标，释义的信息。如果觉得有必要加入生词本的话，那就将他加入到生词本，作者常用的背单词软件是扇贝单词。嗯，思路就是这么的简单，实现的方法无非就是监听剪切板，网络请求提交到扇贝的生词本。下面记录一些实现过程中，遇到的问题及其处理方式，目前仍旧存在的问题。

# V0.1

完成基本的功能。可以选择复制单词之后，弹出单词的释义。按下Add，添加到扇贝生词本。

存在诸多限制：

1. 字典用户自行下载，放在一个指定的目录下才可以使用。
2. Add单词后，即刻网络请求。没有批量。扇贝未收录的单词，没做处理。请求失败，没做处理。
3. 缺少良好的用户权限管理，第一次运行会闪退，因为申请权限是异步的，而未申请到权限就尝试打开文件，故而闪退。
4. 剪切板监听有时候失效，在浏览器里面复制，一定不会弹出单词释义。失效原因不详。
5. 生词本，是在创建视图的时候加载的。两个问题：在主线程进行数据库查询的耗时操作；用户新添加的单词，不会动态的添加进来。
6. 安全问题，比如密码明文保存到本地。csrftoken直接使用抓取的，并且写在代码中。
7. 发起请求有一个cookie字段，除了部分可以获取的信息（auth_token, user_id)外，这个字段里的数据是写死的，来源是自己某一次抓取的内容。
8. 侧边拉开的抽屉，上方的图片是有一个维度是写死的。
9. 用户账号的管理设计有问题

# V0.2

上面列出了诸多限制。这个版本所要做的首要是，更加清晰的代码逻辑，方便后续的维护。其次才是，想办法处理上述限制。

不论再怎么完善，剪切板监听器作为核心功能，在不明原因下失灵，将会导致极其糟糕的体验。解决这个限制才能更好的加强体验。

稍微做了一些调整，现在是稍微能用的程度

1. 将登录相关的东西从MainActivity分离出来，将弹窗和数据库查词分离。
2. 如果用户不给予权限，那么退出程序。
3. Add单词后，发送添加单词请求。如果扇贝未收录，加入生词本

# 使用指引

[使用指引](./user-guide)

# 目前仍旧存在的问题

有时候，不知道什么原因，复制了单词后，只有回到了应用才可以弹出单词释义。

在我的手机上，使用浏览器，当复制了文本之后，在返回到桌面的时候，或者切换应用的时候，才可以弹出单词释义。
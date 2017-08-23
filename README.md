### **图分析系统设计与实现**
***

#### **概述**
***

图分析系统(Graph Analysis System)又名关联分析系统(Association Analysis System)，该系统的研发旨在支持大规模图数据的交互式可视化分析，图数据在当今世界数量极其庞大，怎么利用这些数据成为数据科学家不得不考虑的问题，
该系统解提供了一种解决方案。

#### **功能模块**
***

* 用户管理模块
    * 登陆
    * 注册
    * 权限管理(未完成)
* 数据源模块
    * 支持数据库
        * 目前数据库支持MySQL、PostgreSQL、Greenplum(2017/4/26)、pipelineDB(2017-7-5)，后期会考虑OrientDB作为中间数据库层
            * 还需要尝试TimescaleDB、deepgreen这些基于PostgreSQL 9.5.X版本的数据库
    * 支持文件格式
        * 目前只支持JSON数据格式
* 图展示模块
    * 采用cytoscape.js对从后台读取的JSON数据进行渲染
        * 全部改为使用Sigma.js，效果更好
    * 支持对图中节点、边的一些属性的设置(比如点的形状(star、rectangle等)、点和边的透明度(点和边的透明度越小，显示的图形越模糊))
* 图分析模块
    * 目前支持下面算法
        * firstNeighbors
        * breadthFirstSearch
        * depthFirstSearch
        * pageRank
        * MCL Cluster Algorithm
        * Sampling Algorithm(11个)
* 异常处理模块
    * 当出现异常会给固定的用户发送邮件提示，用户点击的页面会跳转到error提示页面  (2017/4/20)
    * 采用的是Spring的全局异常处理器操作，但是当前版本还不完善的情况下，注释掉了该段代码，目前还是希望能抛出异常，方便调试 (2017/4/20)

#### **版本迭代历史**
***

* 2017/2/28 实现了第一个版本的迭代，其中包含了从数据库中读取数据，并构造JSON字符串，达到了系统基本可用的状态；
* 2017/3/7  实现了第二个版本的迭代，并对前一个版本中出现的bug进行了修复，并在后台实现了计算节点的度，并将节点的度赋值为weight属性；(2017/3/16目前可能会废弃这个版本，极其影响性能，而且目前来看这个操作没意义)
* 2017/3/13 实现了将上述两个版本的代码融合到一个版本中，在DbService.java文件的函数dbDataFormatJson有三个版本，每一个版本的实现的功能请参见相应的Javadoc说明
* 2017/3/15 在分支paging上实现了数据库分页操作，一页页的构造JSON字符串(每次都是发起一个ajax请求)，能将支持渲染的点增加到8000左右
* 2017/4/22 完成数据库的更换，目前支持MySQL、PostgreSQL、Greenplum，在更换数据库之后，得到一个意外的收获，点为7699，边为80000，这样差不多10倍的关系的数据，能够显示出来，并且追踪代码的执行时间，发现需要半分钟显示这些点，慢的原因是浏览器渲染比较慢，但是后台在生成这些点和边的数据时，只需要1s
* 2017/4/23 该版本实现增量加载，提高用户的友好性，选用redis缓存中间计算结果，具体采用redis中的hash数据结构，在该版本中仅仅缓存了sourceNode和targetNode的数据，sourceNode设计的hash结构为(用redis中的语法表示): hset sourceNode "sourceNode:ip地址:数据库名:数据库类型:表名:列名:列值" "节点编号" ;targetNode设计的hash结构为(用redis中的语法表示): hset targetNode "targetNode:ip地址:数据库名:数据库类型:表名:列名:列值" "节点编号"，并且对结果的正确性进行了测试
* 2017/4/26 在increseGetJsonData方法上加锁，使得从客户端发来的请求能一个个的完成，而不是一次一组(通常会是6个SQL请求为一组)，提升了整个系统的交互性能
* 2017/4/26 实现了不分页情况下的数据缓存，减少后台构造字符串的时间，将之前的计算结果缓存到redis中
* 2017/7/5  增加了PipelineDB数据库
* 2017/8/6  增加中间数据库层，该层对用户透明，目的是为了方便算法的实现，比如路径检索算法
* 2017/8/9  完成均匀随机点抽样算法、均匀随机边抽样算法、均匀随机边抽样改进算法、均匀临近点抽样算法，并且证明了均匀随机点抽样改进算法效果出奇的好
* 2017/8/11 完成了不均匀随机点抽样、不均匀随机边抽样算法、基于宽度优先遍历的抽样算法，不均匀是根据点的度来衡量每个点的重要性
* 2017/8/14 完成了基于深度优先遍历的抽样算法，该算法性能很差，几乎没有改进的余地
* 2017/8/15 完成了markov-chain Monte Carlo算法，该抽样算法基于随机游走，当前被认为是最好的算法之一
* 2017/8/16 实现了最基本的基于随机游走的抽样算法、森林火灾抽样算法，随机游走算法本身的性能也是优于其它不是基于随机游走的抽样算法，森林火灾抽样算法
本身是基于BFS的抽样算法，改进后没发现性能有很大的变化
* 2017/8/22 集成了KMeans、EM、DBSCAN、HDBSCAN、KMedoids五个向量聚类算法，用于度的聚类

#### **版本缺点**
***

该版本是目前较为稳定的版本，当然版本存在很多的缺点，下面部分列举实现的具体功能

* 未实现的功能
    * 同一个数据库中不同的表之间能够做连接，连接之后会形成一个大表，这个大表也能做数据分析，下一个版本应该实现这个功能。
    * 算法改进(目前在研究markov算法，完成之后将用Java实现，并想办法改进，使得其符合我的系统要求 2017/5/1提出该问题)
    * 设计系统并使用OrientDB实现检索功能(2017/5/1提出该问题，在本月月末必须将该功能增加到**新增功能**列表中)
* 已经实现的功能，但是功能存在缺点
    * 目前数据库只支持MySQL数据库，PostgreSQL数据库在使用时和MySQL有比较大的区别，下一个版本为了能从分布式集群(Greenplum集群)中读取数据并构建图，所以下一个版本使用PostgreSQL数据库；(2017/4/22实现数据库支持的扩展，目前支持PostgreSQL、Greenplum、MySQL)
    * 目前仅仅支持3000个点、3000条边左右的数据量，从当前数据量的情况，这远远没达到大规模图数据分析的要求，下一步采用增量技术实现对大规模数据集的支持；(2017/3/17 能将点增加到8000左右，这是一次比较重要的版本迭代；2017/4/22 能够将显示的点增加到8000，边增加到80000，点边比例为10，这意味着可能效果甚至会比这个更好)
    * 目前仅仅支持3000个点、3000条边左右的数据量，从当前数据量的情况，这远远没达到大规模图数据分析的要求，下一步采用增量技术实现对大规模数据集的支持；(2017/3/17 能将点增加到8000左右，这是一次比较重要的版本迭代；2017/4/22 能够将显示的点增加到8000，边增加到80000，点边比例为10，这意味着可能效果甚至会比这个更好)
    * 当某一列的节点重复率(节点的重复率是指某一个值在该列出现不止一次)越高，构造JSON字符串的性能就越差(在实际的测试中对比，有2000个点、2500条边时，需要大致6s才能构造成JSON字符串，渲染这些JSON数据需要2s至少 已经改进 2017/5/1 2017/5/17出现这种情况的原因是之前的代码存在隐藏的bug，现在已经不存在这个问题了)；
    * 整个系统的UI设计严重存在缺陷，需要改进
    * pagerank需要节点的度和迭代次数，没有设计，应该增加
    * 增加了大量的抽样算法，算法本身都是全世界目前在用的最优秀的算法，基本都出自KDD、TKDD、IMC等顶级会议发表的文章

#### **新增功能**
***

* 2017/2/10-2017/3/10  实现了图数据分析系统基本模型，得到系统基本可用状态
* 2017/3/14-2017/3/15 实现了数据库分页，一页页的将数据构造成JSON格式并传递到前台渲染(获取每一页的数据都发起一次ajax请求)，但是存在bug，需要使用redis数据库作为缓存才行，bug修复(2017/4/23，该版本已经可用)
* 2017/4/10-2017/4/19 完成redis数据库的基本使用，以及设计存储中间计算结果的数据结构，在redis中决定采用hash作为缓存数据结构。（2017/4/23完成该项功能）
* 2017/4/23 设计了redis缓存结构，并将其应用到图分析系统中，用于缓存中间计算结果
* 2017/4/26 给增量加载方法加锁，从而实现了真正的增量加载
* 2017/4/26 当用户不选择增量加载，而是一次性加载全部的数据时，采用redis缓存相同ip、相同SQL的计算结果，从而使得整个过程更加快速，并且不做多余的计算
* 2017/8/6  增加中间数据库层，该层对用户透明，目的是为了方便算法的实现，比如路径检索算法
* 2017/8/9  完成点抽样算法、边抽样算法、边抽样改进算法、拓扑抽样算法，并且证明了边抽样改进算法和拓扑抽样算法的性能最佳
* 2017/8/11 完成了不均匀随机点抽样、不均匀随机边抽样算法、基于拓扑结构的抽样，不均匀是根据点的度来衡量每个点的重要性
* 2017/8/14 完成了基于深度优先遍历的抽样算法，该算法性能很差，几乎没有改进的余地
* 2017/8/15 完成了markov-chain Monte Carlo算法，该抽样算法基于随机游走，当前被认为是最好的算法之一
* 2017/8/16 实现了最基本的基于随机游走的抽样算法、森林火灾抽样算法，随机游走算法本身的性能也是优于其它不是基于随机游走的抽样算法，森林火灾抽样算法
本身是基于BFS的抽样算法，改进后没发现性能有很大的变化
* 2017/8/22 集成了KMeans、EM、DBSCAN、HDBSCAN、KMedoids五个向量聚类算法，用于度的聚类







# EasyAmmeter
房东电费管理助手，根据分表计算个租客应缴电费
页面结构：
  + <a name="首页">【首页】 </a>
    - [余额](#录入)（输入总表账号当前余额）
    - [新增缴费](#录入)（距离上次结算后，每次实际缴费到账户都要新增缴费，会生成缴费记录）
    - [缴费记录](#记录列表)（所有新增缴费后生成的缴费记录列表）
    - [租户入住](#录入)（新租户入住需要录入租户分电表的数据，会默认给租户一个名称<租户N>，N表示根据实际情况递增数字，从1开始）
    - [我的租户](#租户列表)（进入所有租户的列表）
    - [结算电费](#总表数据)（随时可以进行电费结算，结清当前节点之前的所有电费使用情况，第一次使用也是默认进入结算页面）
  
  + <a name="记录列表">【记录列表】 </a>（显示每次缴费时间和金额，单击确定返回上一页面）（显示每次充值时间和金额，单击确定返回上一页面）
  
  + <a name="总表数据">【总表数据】 </a>（确保总表余额和电表数据正确）
    - [余额](#录入)
    - [电表数据](#录入)
    - [继续](#分表数据)（进入分表数据确认和输入）
  
  + <a name="分表数据">【分表数据】 </a>（分表输入每个租户的分表数据）
    - [结算](#结算确认)（计算当前所以租户应该支付的电费并跳转到结算确认页面）
  
  + <a name="结算确认">【结算确认】 </a>（显示所有租户的电费计算详情，需要房主确认数据，本次数据会保留，参与到下次结算的计算，因此要慎重。）
  
  + <a name="录入">【数据录入】 </a>（输入数据并返回）
  
  + <a name="租户列表">【租户列表】 </a>（显示每个租户名和当前缴费情况）
    - [租户详情](#租户详情)
    
  + <a name="租户详情">【租户详情】 </a>（显示每个租户名和当前缴费情况）
    - 余额（显示账号当前余额）
    - [修改名称](#录入)（修改租户的名称）
    - [充值缴费](#录入)（租户随时进行充值，会生成充值记录）
    - [充值记录](#记录列表)（所有充值缴费后生成的充值记录列表）
    - [电表数据](#录入)（录入当前电表数据）
    - 退租（退租前必须进行结算，确保所有电费已经结算清楚）
      
 
# 使用说明
+ 未初始化时（默认会直接打开初始化界面<结算页面>）
    #### 初始化数据
    1）、金额 -> 输入总电表金额
    
       通过支付宝或者微信绑定查看总电表余额，单位元

    2）、电表 -> 输入总电表当前读数

       通过看总电表的读数，单位度

    3）、完成

       初始化数据完成，进入主页，进行租户入住操作
       
 + 已经初始化
     #### 打开应用默认进入管理首页
      1）、任何时候都可以查看租户情况（电费余额）。
      
      2）、任何时候都可以收取租户预存电费。
      
      3）、任何时候都可以缴纳电费，确保不会断电。
      
      4）、任何时候都可以结算之前的数据，数据初始化，新租户入住前，退租前都可以进行电费结算（结算的时候要确保每个电表数据都是正确的）。
      
      5）、每次结算都只需要输入每个租户的分表数据，总表数据以及总表余额，其他数据都是平时维护的。

# 计算说明
  + 说明 
 分表用小写，总表大写，下标数字₁表示上一次，下标数字₂表示当前，上标ⁿ表示分表N
 
|        符号             |               说明                   |
| ---------------------  |  ----------------------------------  |
|    - M       | 金额/余额  |
|    - ∆M      | 实际使用金额 |
|    - ∑M      | 总表累计缴费金额 |
|    - ⁿ∑m     | 各分表累计缴费金额 |
|    - ⁿ〒m     | 公摊金额 |
| -----------------------|--------------------------------  |
|    - A       | 电量 |
|    - ∆A      | 实际使用电量 |
|    - ∆a      | 分表实际使用电量 |
|    - ∑a      | 分表用电总和 |
|    - 〒A     | 公摊电量 |
|    - P       | 单价 |

  + 计算公式
  > 实际使用金额等于房东累计交的钱减去账户剩余的钱，之前余额也是房东交的钱
   ```∆M = ∑M + M₁ - M₂```
  
  > 实际使用电量等于两次电表差额
    ```∆A = A₂ - A₁```
    ```ⁿ∆a = ⁿa₂ - ⁿa₁```
  
  > 这段时间均价等于总金额除以总电量
    ```P = ∆M/∆A```
  
  > 分表用电总和等于各个分表电量相加
   ```∑a = ¹∆a +²∆a +...+ ⁿ∆a```
  
  > 公摊电量等于总电量减去分表电量除以分表数
   ```〒A = (∆A - ∑a) / N```
  
  > 公摊金额等于公摊电量乘以单价
   ```ⁿ〒m = 〒A * P```
  
  > 个人金额等于个人电量乘以单价
    ```ⁿm₂ =ⁿ∆a * P₂```
  
  > 个人总金额等于个人电量乘以单价
    ```ⁿ∑m₂ =ⁿ∆a * P₂```

# 欢迎 Star 和提交 Issue


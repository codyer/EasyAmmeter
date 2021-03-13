# EasyAmmeter
    使用简单的 M-V-VM 架构，实现房东电费管理助手，根据分表计算个租客应缴电费

    M   ->  Model
    V   ->  UI
    VM  ->  ViewModel

# 背景
    合租房时，需要分表记录电表数据，公摊电费，每次都要计算，实际都是固定计算方式，为了更便捷计算，且可以查到每次
    结算记录，也正好验证一下《component》组件快速开发的能力，因此才有了这个项目。

# 截屏预览
<img width="20%" src="https://tva1.sinaimg.cn/large/008eGmZEgy1goidh3a4mbj30u01t0127.jpg"><img width="20%" src="https://tva1.sinaimg.cn/large/008eGmZEgy1goidh2y8p0j30u01t00yo.jpg"><img width="20%" src="https://tva1.sinaimg.cn/large/008eGmZEgy1goidh3smerj30u01t0aqz.jpg">
<img width="20%" src="https://tva1.sinaimg.cn/large/008eGmZEgy1goidh3izvoj30u01t0wkd.jpg">
<div width="20%">首页</div><div width="20%">我的租客</div><div width="20%">输入</div><div width="20%">历史结算</div>

# 下载地址：
  1）[蒲公英地址](https://www.pgyer.com/5Mq5)

# 使用说明
    1、每次充值输入缴费金额（计算要用）
    2、每次收取租客电费输入租客的充值金额（记录用户累计欠费缴费情况）
    3、每次抄表结算输入总表账户当前余额和所有电表的数据（计算每次/每月电费详情）

# 使用第三方
    1、component
    2、ROOM
    3、data binding
    4、更多可以参考component

# 计算说明

  + 说明 
    分表用小写，总表大写，下标数字₁表示上一次，下标数字₂表示当前，上标ⁿ表示分表N

    |        符号             |               说明                   |
    | ---------------------  |  ----------------------------------  |
    |     M       | 金额/余额  |
    |     ∆M      | 实际使用金额 |
    |     ∑M      | 总表累计缴费金额 |
    |     ⁿ∑m     | 各分表总计应缴费金额 |
    |     ⁿ〒m     | 公摊金额 |
    |    A       | 电量 |
    |    ∆A      | 实际使用电量 |
    |    ⁿ∆a      | 分表实际使用电量 |
    |    ∑∆a      | 分表用电总和 |
    |    〒A     | 公摊电量 |
    |    P       | 单价 |

      + 计算公式

        - 实际使用金额等于房东累计交的钱减去账户剩余的钱，之前余额也是房东交的钱

            ∆M = ∑M + M₁ - M₂

        - 实际使用电量等于两次电表差额

            ∆A = A₂ - A₁
            ⁿ∆a = ⁿa₂ - ⁿa₁

        - 这段时间均价等于总金额除以总电量

            P = ∆M/∆A

        - 分表用电总和等于各个分表电量相加

            ∑∆a = ¹∆a +²∆a +...+ ⁿ∆a

        - 人均公摊电量等于总电量减去分表电量除以分表数

            〒A = (∆A - ∑∆a) / N

        - 公摊金额等于公摊电量乘以单价

            ⁿ〒m = 〒A * P

        - 分表金额等于分表电量乘以单价

            ⁿm =ⁿ∆a * P

        - 个人总金额等于分表电量加公摊电量，再乘以单价

            ⁿ∑m = （〒A  + ⁿ∆a） * P


# 欢迎 Star 和提交 Issue


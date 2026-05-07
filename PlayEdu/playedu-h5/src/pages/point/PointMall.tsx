import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Button,
  Card,
  Dialog,
  Empty,
  NavBar,
  SpinLoading,
  Tag,
  Toast,
} from "antd-mobile";
import { getBalance, listProducts, type PointProduct } from "../../api/point";
import styles from "./point.module.scss";

const PointMallPage = () => {
  const navigate = useNavigate();
  const [balance, setBalance] = useState(0);
  const [products, setProducts] = useState<PointProduct[]>([]);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      setLoading(true);
      const [balanceRes, productRes]: any = await Promise.all([
        getBalance(),
        listProducts(),
      ]);
      if (balanceRes.code === "0" || balanceRes.code === 0) {
        setBalance(balanceRes.data || 0);
      }
      if (productRes.code === "0" || productRes.code === 0) {
        setProducts(productRes.data || []);
      }
    } catch (error) {
      Toast.show({ icon: "fail", content: "加载失败" });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    document.title = "积分商城";
    void loadData();
  }, []);

  const handleExchange = async (product: PointProduct) => {
    if (product.stock <= 0) {
      Toast.show({ icon: "fail", content: "该商品已售罄" });
      return;
    }
    if (balance < product.pointsPrice) {
      Toast.show({ icon: "fail", content: "积分不足" });
      return;
    }

    const confirmed = await Dialog.confirm({
      title: "确认兑换",
      content: `使用 ${product.pointsPrice} 积分兑换「${product.name}」？`,
      confirmText: "确认兑换",
      cancelText: "取消",
    });

    if (!confirmed) {
      return;
    }

    setBalance((current) => current - product.pointsPrice);
    setProducts((current) =>
      current.map((item) =>
        item.id === product.id ? { ...item, stock: Math.max(0, item.stock - 1) } : item
      )
    );
    Toast.show({ icon: "success", content: "兑换成功" });
  };

  return (
    <div className={styles.pointPage}>
      <NavBar
        back={null}
        right={
          <div
            onClick={() => navigate("/points/records")}
            className={styles.navRight}
          >
            <span className={styles.inlineMark}>明</span>
            明细
          </div>
        }
      >
        积分商城
      </NavBar>

      {loading ? (
        <div className={styles.loadingWrap}>
          <SpinLoading color="primary" />
        </div>
      ) : (
        <>
          <div className={styles.balanceCard}>
            <div className={styles.balanceLabel}>我的积分</div>
            <div className={styles.balanceValue}>
              <span className={styles.coinMark}>积</span>
              <span>{balance}</span>
            </div>
            <div className={styles.balanceHint}>完成考试和课程可获取更多积分</div>
          </div>

          <div className={styles.productSection}>
            <div className={styles.sectionTitle}>
              <span className={styles.inlineMark}>礼</span>
              积分兑换
            </div>
            {products.length === 0 ? (
              <Empty description="暂无商品" />
            ) : (
              <div className={styles.productGrid}>
                {products.map((product) => (
                  <Card key={product.id} className={styles.productCard}>
                    <div className={styles.productImage}>
                      {product.imageUrl ? (
                        <img src={product.imageUrl} alt={product.name} />
                      ) : (
                        <div className={styles.placeholderImg}>暂无图片</div>
                      )}
                    </div>
                    <div className={styles.productName}>{product.name}</div>
                    <div className={styles.productDesc}>
                      {product.description || "暂无商品说明"}
                    </div>
                    <div className={styles.productFooter}>
                      <span className={styles.productPrice}>
                        <span className={styles.priceMark}>积</span>
                        {product.pointsPrice}
                      </span>
                      <Tag color={product.stock > 0 ? "success" : "default"}>
                        {product.stock > 0 ? `库存 ${product.stock}` : "售罄"}
                      </Tag>
                    </div>
                    <Button
                      block
                      color="primary"
                      size="small"
                      className={styles.exchangeBtn}
                      disabled={
                        product.stock <= 0 || balance < product.pointsPrice
                      }
                      onClick={() => void handleExchange(product)}
                    >
                      {product.stock <= 0
                        ? "已售罄"
                        : balance < product.pointsPrice
                          ? "积分不足"
                          : "立即兑换"}
                    </Button>
                  </Card>
                ))}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
};

export default PointMallPage;

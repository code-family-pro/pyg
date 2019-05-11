package pojogroup;

import cn.itcast.core.pojo.order.OrderItem;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Cart implements Serializable {

    private String SellerID;
    private String SellerName;
    private List<OrderItem> OrderItemList;

    public String getSellerID() {
        return SellerID;
    }

    public void setSellerID(String sellerID) {
        SellerID = sellerID;
    }

    public String getSellerName() {
        return SellerName;
    }

    public void setSellerName(String sellerName) {
        SellerName = sellerName;
    }

    public List<OrderItem> getOrderItemList() {
        return OrderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        OrderItemList = orderItemList;
    }


    @Override
    public String toString() {
        return "Cart{" +
                "SellerID='" + SellerID + '\'' +
                ", SellerName='" + SellerName + '\'' +
                ", OrderItemList=" + OrderItemList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return Objects.equals(SellerID, cart.SellerID);
    }

    @Override
    public int hashCode() {

        return Objects.hash(SellerID);
    }
}

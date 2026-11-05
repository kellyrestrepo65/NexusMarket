import json, sys, time, urllib.request, urllib.error

BASE = "http://localhost:8080/api"
ADMIN, OPER, SUP = sys.argv[1], sys.argv[2], sys.argv[3]
ok = fail = 0


def call(method, path, actor=None, body=None, expect=200):
    global ok, fail
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if actor:
        req.add_header("X-User-Id", actor)
    try:
        with urllib.request.urlopen(req) as r:
            status, text = r.status, r.read().decode()
    except urllib.error.HTTPError as e:
        status, text = e.code, e.read().decode()
    good = (status == expect) if isinstance(expect, int) else status in expect
    ok += good
    fail += not good
    print("%s %-4s %-45s -> %d %s" % ("OK " if good else "BAD", method, path, status, "" if good else text[:200]))
    try:
        return json.loads(text) if text else None
    except ValueError:
        return None


s = str(int(time.time()))[-6:]
seller = call("POST", "/users/sellers", ADMIN, {"identityDocument": "S" + s, "fullName": "Seller Demo",
              "email": "seller%s@demo.com" % s, "firstWarehouseName": "Bodega Vendedor"}, (200, 201))
buyer = call("POST", "/users/buyers", None, {"identityDocument": "B" + s, "fullName": "Buyer Demo",
             "email": "buyer%s@demo.com" % s, "mainAddress": {"addressLine1": "Calle 1", "city": "Medellin",
             "country": "CO"}}, (200, 201))
SEL, BUY = seller["id"], buyer["id"]
call("POST", "/users/sellers", BUY, {"identityDocument": "X" + s, "fullName": "X", "email": "x%s@d.com" % s,
     "firstWarehouseName": "W"}, 403)
wh = call("GET", "/warehouses", SEL)[0]
prod = call("POST", "/products", SEL, {"name": "Laptop", "price": 1000, "type": "PHYSICAL",
            "variants": {"color": "negro"}}, (200, 201))
call("GET", "/products", BUY)
inv = call("POST", "/inventories/receipts", SEL, {"productId": prod["id"], "warehouseId": wh["id"], "quantity": 10}, (200, 201))
call("POST", "/inventories/%s/adjustments" % inv["id"], OPER, {"delta": -1})
call("POST", "/inventories/%s/damaged" % inv["id"], SEL, {"quantity": 1})
call("GET", "/inventories?productId=" + prod["id"], SUP)
call("POST", "/cart/items", BUY, {"productId": prod["id"], "quantity": 2}, (200, 201))
call("PUT", "/cart/items/" + prod["id"], BUY, {"quantity": 3})
call("GET", "/cart", BUY)
call("PUT", "/cart/items/" + prod["id"], BUY, {"quantity": 999})
call("POST", "/orders", BUY, None, 422)
call("PUT", "/cart/items/" + prod["id"], BUY, {"quantity": 3})
order = call("POST", "/orders", BUY, None, (200, 201))
OID = order["id"]
call("POST", "/orders/%s/payment" % OID, BUY)
call("POST", "/orders/%s/payment" % OID, BUY, None, (409, 422, 400))
call("GET", "/orders/%s/invoice" % OID, BUY)
call("POST", "/orders/%s/dispatch" % OID, SEL, None, 403)
call("POST", "/orders/%s/dispatch" % OID, OPER)
call("POST", "/orders/%s/delivery" % OID, OPER)
call("GET", "/orders/%s/shipment" % OID, BUY)
ret = call("POST", "/returns", BUY, {"orderId": OID, "reason": "Llego roto"}, (200, 201))
call("POST", "/returns/%s/approval" % ret["id"], ADMIN)
call("POST", "/returns/%s/refund/processing" % ret["id"], ADMIN)
call("GET", "/returns", BUY)
call("GET", "/reports/administrative", SUP)
call("GET", "/reports/administrative", BUY, None, 403)
call("POST", "/products", SUP, {"name": "X", "price": 1, "type": "DIGITAL"}, 403)
call("GET", "/users", ADMIN)
call("GET", "/orders", None, None, (400, 401))
print("\nOK=%d BAD=%d" % (ok, fail))

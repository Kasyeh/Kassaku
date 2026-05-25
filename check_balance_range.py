import sys

def check_balance(filename, start, end):
    balance = 0
    with open(filename, 'r') as f:
        for i, line in enumerate(f, 1):
            opens = line.count('{')
            closes = line.count('}')
            balance += opens - closes
            if i >= start and i <= end:
                print(f"Line {i}: balance {balance} (+{opens}, -{closes})")

if __name__ == "__main__":
    check_balance(sys.argv[1], int(sys.argv[2]), int(sys.argv[3]))

import sys

def check_balance(filename):
    balance = 0
    with open(filename, 'r') as f:
        for i, line in enumerate(f, 1):
            opens = line.count('{')
            closes = line.count('}')
            balance += opens - closes
            if balance < 0:
                print(f"Negative balance at line {i}: {balance}")
            if i % 100 == 0:
                print(f"Line {i}: balance {balance}")
    print(f"Final balance: {balance}")

if __name__ == "__main__":
    check_balance(sys.argv[1])
